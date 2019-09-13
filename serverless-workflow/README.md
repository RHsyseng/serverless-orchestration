# Serverless Orchestration Service

## Build

* Uberjar builds

```shell script
mvn clean package
```

* Native builds: See [Quarkus: Building a native executable](https://quarkus.io/guides/building-native-image-guide)

```shell script
mvn clean package -Pnative
```

## Run

### Environment variables

* Define the `WORKFLOW_PATH` environment variable that points to the file containing
the json definition of the workflow

```shell script
WORKFLOW_PATH=/opt/workflows/example-workflow.json
```

### Run the uberjar locally

```shell script
java -jar target/serverless-wf-1.0-SNAPSHOT-runner.jar
```

### Run the native binary

```shell script
./target/serverless-wf-1.0-SNAPSHOT-runner
```

# Deploy on Kubernetes

The [deploy](deploy) folder contains a `deployment.yaml` file with 
a predefined environment variable and a volume mounting a configmap.

The configmap can be created using the following command:

```shell script
$ kubectl create cm --from-file workflow.json=deploy/examples/age-evaluation.json workflow-service
configmap/workflow-service created
```

Adding a label would help identifying the resource
```shell script
$ kubectl label configmap workflow-service app=workflow-service
configmap/workflow-service labeled
```

The other resources can be created all together:

```shell script
$ kubectl create -f deploy    
deployment.extensions/workflow-service created
ingress.networking.k8s.io/workflow-service created
service/workflow-service created
```

To remove all the resources we can use the following commands:

```shell script
$ kubectl delete cm,svc,ingress,deployment -l app=workflow-service
configmap "workflow-service" deleted
service "workflow-service" deleted
ingress.extensions "workflow-service" deleted
deployment.extensions "workflow-service" deleted
``` 