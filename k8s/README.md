# Kubernetes setup for CRMHybridAutomationFrameWork

Five objects, applied in this order. Everything lives in the `qa-automation`
namespace so a `kubectl delete namespace qa-automation` cleans the lot.

| File | Object | What it does |
|---|---|---|
| `00-namespace.yaml` | Namespace | Isolates the grid and the tests from the rest of the cluster |
| `01-configmap.yaml` | ConfigMap | Injects `ENV`, `BROWSER`, `REMOTE`, `HUB_URL`, `URL` etc. as env vars. `DriverFactory.initProp()` reads them and they override the `.properties` files |
| `02-secret.example.yaml` | Secret (template) | Holds the CRM username/password. **Copy to `02-secret.yaml`, fill in, apply that. The real file is gitignored** |
| `03-grid-hub.yaml` | Service + Deployment | The Grid 4 hub. The Service name `selenium-hub` is the hostname the framework uses |
| `04-grid-node.yaml` | Deployment | Chrome nodes that register with the hub. `replicas` = parallel capacity |
| `05-test-job.yaml` | Job | Runs `mvn test`, exits 0 or 1. This is what CI triggers |
| `06-pvc.yaml` | PersistentVolumeClaim | Shared volume so reports survive the pod |

## Deploy

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-configmap.yaml
kubectl apply -f k8s/06-pvc.yaml
kubectl apply -f k8s/02-secret.example.yaml    # template only, use 02-secret.yaml locally
kubectl apply -f k8s/03-grid-hub.yaml
kubectl apply -f k8s/04-grid-node.yaml
kubectl apply -f k8s/05-test-job.yaml
```

Watch it:

```bash
kubectl -n qa-automation get pods -w
kubectl -n qa-automation logs job/crm-regression -f
```

Grid console (port-forward, there is no public LoadBalancer):

```bash
kubectl -n qa-automation port-forward svc/selenium-hub 4444:4444
# open http://localhost:4444/grid/console
```

Clean up:

```bash
kubectl delete namespace qa-automation
```

## Re-run after a code change

A Job is immutable, so delete it first:

```bash
kubectl -n qa-automation delete job crm-regression --ignore-not-found
kubectl apply -f k8s/05-test-job.yaml
```

## Build and push the test image

The Job cannot build, it only runs an image. Push it first:

```bash
docker build -t ghcr.io/madhusudan-1990/crm-tests:1.0 .
docker push ghcr.io/madhusudan-1990/crm-tests:1.0
```

## Three things that catch people out

1. **`/dev/shm` is 64 MB by default** and Chrome crashes with
   `Tab crashed`. `04-grid-node.yaml` mounts an `emptyDir` with
   `medium: Memory` and `sizeLimit: 2Gi` at `/dev/shm`.
2. **`fsGroup: 10001`** in the Job is what lets the non-root image user write
   into the PVC. Without it the run fails with `Permission denied` on
   `/app/reports`.
3. **A Job cannot be edited.** `kubectl apply` on a modified Job is a no-op.
   Delete it first (see above), or bump `metadata.name` to `crm-regression-2`.
