# Infrastructure

Terraform that deploys the Franchise API to AWS: ECS Fargate behind an Application Load Balancer, RDS PostgreSQL with its
credentials in Secrets Manager, and the image in ECR.

## Layout

```
iac/
├── bootstrap/                 # one-time: S3 bucket that stores the remote state (local state)
├── modules/                   # reusable building blocks
│   ├── network/               # VPC, public and private subnets, NAT, S3 gateway endpoint, optional flow logs
│   ├── ecs-cluster/           # ECS cluster with Fargate and Fargate Spot
│   ├── alb/                   # internet-facing load balancer, HTTP listener, optional HTTPS
│   ├── database/              # RDS PostgreSQL with an RDS-managed secret
│   ├── ecr/                   # image repository with immutable tags
│   └── ecs-service/           # task definition, service, IAM roles, target group, auto scaling, logs
├── stacks/
│   ├── transversal/           # shared infrastructure of an environment
│   └── franchise-api/         # the service of an environment
│       ├── env/<env>/terraform-<env>.tfvars
│       └── env/<env>/backend.hcl
└── scripts/                   # bootstrap, deploy, publish-image, destroy
```

| Root | Creates | State key |
|---|---|---|
| `bootstrap` | State bucket: versioned, encrypted, public access blocked, TLS-only | local |
| `stacks/transversal` | VPC, ECS cluster, ALB, RDS, ECR | `franchise-api/transversal/<env>/terraform.tfstate` |
| `stacks/franchise-api` | ECS service, task and execution roles, target group and listener rule, auto scaling, logs, database ingress rule | `franchise-api/franchise-api/<env>/terraform.tfstate` |

- **Environments:** `dev`, `qa` and `pdn` share the modules. Values differ in `env/<env>/terraform-<env>.tfvars`, and the
  backend key in `env/<env>/backend.hcl`.
- **State:** S3 backend with native locking (`use_lockfile`), so no DynamoDB table is needed.
- **Linking stacks:** the service stack finds the transversal resources by name and tag through `data` sources.
- **Secrets:** RDS generates the master password in Secrets Manager. The container receives only the secret ARN and
  reads the secret with its task role, which may read that secret alone.
- **Application settings:** values derived from the infrastructure (database host, secret ARN, region) are built in
  `locals.tf`. Settings tuned per environment (timeouts, pool, circuit breaker, retry, log levels) are listed in
  `app_environment` of each tfvars; any variable left out uses the default of `application.yaml`.
- **Defaults:** modules are secure by default (deletion protection, final snapshot, backups). `dev` relaxes them in its
  tfvars so it can be destroyed after use.

## Usage

Requirements: AWS CLI with credentials for the target account, Terraform >= 1.10, Docker or Podman, and Bash (Git Bash
on Windows).

```bash
./iac/scripts/bootstrap.sh       # once per account
./iac/scripts/deploy.sh dev      # transversal stack -> image build and push -> service stack
./iac/scripts/destroy.sh dev     # service stack, then transversal stack
```

- `deploy.sh` tags the image with the current commit. ECR tags are immutable, so commit your changes first.
- `deploy.sh` prints the API, Swagger and health URLs when it finishes, and writes the API URL to `docs/bruno/.env` for the Bruno `aws` environment.
- `apply` and `destroy` ask for confirmation unless `AUTO_APPROVE=true`.
- To use another Terraform binary, set `TERRAFORM=/path/to/terraform`.

HTTPS is enabled by setting `alb_certificate_arn` in the transversal tfvars (an ACM certificate for your domain) and
`listener_port = 443` in the service tfvars.
