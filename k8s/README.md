# Kubernetes setup for CRMHybridAutomationFrameWork

Four objects, applied in this order. Everything lives in the `qa-automation`
namespace so a `kubectl delete namespace qa-automation` cleans the lot.

| File | Object | What it does |
|---|---|---|
| `00-namespace.yaml` | Namespace | Isolates the test runs from the rest of the cluster |
| `01-configmap.yaml` | ConfigMap | Injects `ENV`, `BROWSER`, `URL`, `SUITE_XML`, `SHARD_COUNT` etc. as env vars. `DriverFactory.initProp()` reads them and they override the `.properties` files |
| `02-secret.example.yaml` | Secret (template) | Holds the CRM username/password. **Copy to `02-secret.yaml`, fill in, apply that. The real file is gitignored** |
| `05-test-job.yaml` | Indexed Job | Runs the suite in 4 parallel shards. This is what CI triggers |
| `06-pvc.yaml` | PersistentVolumeClaim | Shared volume so reports survive the pods |

There is **no Selenium Grid** in this setup. The image ships headless Chrome, so
each pod is a self-contained test environment.

## Deploy

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-configmap.yaml
kubectl apply -f k8s/06-pvc.yaml
kubectl apply -f k8s/02-secret.example.yaml    # template only, use 02-secret.yaml locally
kubectl apply -f k8s/05-test-job.yaml
```

Watch it:

```bash
kubectl -n qa-automation get pods -w
kubectl -n qa-automation logs job/crm-regression -f
```

## How the sharding works

`05-test-job.yaml` sets `completionMode: Indexed` with `parallelism: 4` and
`completions: 4`. Kubernetes gives each pod a label
`batch.kubernetes.io/job-completion-index` from 0 to 3, which is projected into
the `SHARD_INDEX` env var.

`ShardInterceptor` then keeps only the methods where
`methodIndex % SHARD_COUNT == SHARD_INDEX`. Four pods, four disjoint slices, one
suite, no pod running the same test as another.

To change the split, edit **both** `parallelism`/`completions` in
`05-test-job.yaml` and `SHARD_COUNT` in `01-configmap.yaml`. Keeping them out of
sync means empty shards or a suite that never finishes.

## Build and push the test image

The Job cannot build, it only runs an image. Push it first:

```bash
docker build -t ghcr.io/madhusudan-1990/crm-tests:1.0 .
docker push ghcr.io/madhusudan-1990/crm-tests:1.0
```

## Re-run after a code change

A Job is immutable, so delete it first:

```bash
kubectl -n qa-automation delete job crm-regression --ignore-not-found
kubectl apply -f k8s/05-test-job.yaml
```

Clean up:

```bash
kubectl delete namespace qa-automation
```

## Three things that catch people out

1. **`/dev/shm` is 64 MB by default** and Chrome crashes with `Tab crashed`.
   Compose sets `shm_size: 2gb`. Inside Kubernetes add an `emptyDir` with
   `medium: Memory` mounted at `/dev/shm`, or Chrome will die part way through.
2. **`fsGroup: 10001`** in the Job is what lets the non-root image user write
   into the PVC. Without it the run fails with `Permission denied` on
   `/app/tests/reports`.
3. **A Job cannot be edited.** `kubectl apply` on a modified Job is a no-op.
   Delete it first (see above), or bump `metadata.name` to `crm-regression-2`.
