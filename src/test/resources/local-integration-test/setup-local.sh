function main()
{
  ./start-k3s.sh

  sudo k3s kubectl apply -f https://github.com/jetstack/cert-manager/releases/download/v1.5.4/cert-manager.yaml

}

main
