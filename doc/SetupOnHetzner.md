# Setup 
## Infrastructure on Hetzner / Aws

For a setup on hetzner / aws we use terraform.

```
resource "aws_s3_bucket" "backup" {
  bucket = "backup"
  acl    = "private"

  versioning {
    enabled = false
  }
  tags = {
    name        = "backup"
    Description = "bucket for backups in stage: ${var.stage}"
  }
}

resource "hcloud_server" "cloud_09_2021" {
  name        = "the name"
  image       = "ubuntu-20.04"
  server_type = "cx31"
  location    = "fsn1"
  ssh_keys    = ...

  lifecycle {
    ignore_changes        = [ssh_keys]
  }
}

resource "aws_route53_record" "v4_neu" {
  zone_id = the_dns_zone
  name    = "cloud-neu"
  type    = "A"
  ttl     = "300"
  records = [hcloud_server.cloud_09_2021.ipv4_address]
}

output "ipv4" {
  value = hcloud_server.cloud_09_2021.ipv4_address
}

```

## k8s minicluster

For k8s installation we use our [provs](https://repo.prod.meissa.de/meissa/provs) with the following configuation:


```
postgres-db-user: "nextcloud"
postgres-db-password: "nextcloud-db-password"
nextcloud-admin-user: "cloudadmin"
nextcloud-admin-password: "cloudpassword"
aws-access-key-id: "aws-id"
aws-secret-access-key: "aws-secret"
restic-password: "restic-password"
```

## kubectl apply c4k-nextcloud

The last step for applying the nextcloud deployment is

```
c4k-nextcloud config.edn auth.edn | kubectl apply -f -
```

with the following config.edn:

```
{:fqdn "the-fqdn-from aws_route53_record.v4_neu"
 :nextcloud-data-volume-path "/var/cloud"                 ;; Volume was configured at dda-k8s-crate, results in a PersistentVolume definition.
 :postgres-data-volume-path "/var/postgres"         ;; Volume was configured at dda-k8s-crate, results in a PersistentVolume definition.
 :restic-repository "s3:s3.amazonaws.com/your-bucket/your-folder"}
```
