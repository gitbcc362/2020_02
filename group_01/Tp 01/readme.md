# Visualizacao do dashboard:
## No CMD:
	kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.2.0/aio/deploy/recommended.yaml

	kubectl proxy

## No GCP:

	gcloud auth application-default print-access-token



# Links uteis no dashboard local:

	http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/node?namespace=default

	http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/replicaset/ignite/ignite-cluster-59bf6959bd?namespace=ignite

	http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/pod?namespace=ignite

# Redimensionamento do cluster:

	gcloud init

	gcloud container clusters resize cluster-2 --node-pool default-pool --num-nodes 3

# Escalonamento de pods:

	kubectl scale deployment ignite-cluster --replicas=3 -n ignite
	kubectl scale sts ignite-cluster --replicas=3 -n ign
