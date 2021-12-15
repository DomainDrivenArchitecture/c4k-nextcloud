KUBECONFIG=~/.kube/custom-contexts/k3d-config.yml k3d cluster create nextcloud --k3s-arg '--tls-san cloudhost@loadbalancer' --port 80:80@loadbalancer --port 443:443@loadbalancer --api-port 6443 --kubeconfig-update-default