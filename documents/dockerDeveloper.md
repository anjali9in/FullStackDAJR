To create a Docker file, Need the following things:

1. base image.  - FROM <base_image>:<tag>
2. working directory. - WORKDIR {path}
3. copy files from local to container.  - COPY <source> <destination>
4. expose port. - EXPOSE <port>
5. start up command.  - CMD ["executable","param1","param2"] OR ENTRYPOINT ["executable","param1","param2"]

6. create and build image.  - docker build -t <image_name>:<tag> .
(dot means directory where docker file is present.)
(-t means create tag name of image.)

7. run image in container.  - docker run -p <host_port>:<container_port> <image_name>:<tag>

  a. to run in detached mode, use -d flag.  -
    docker run -d -p <host_port>:<container_port> <image_name>:<tag>

  b. to run in interactive mode, use -it flag.  -
    docker run -it -p <host_port>:<container_port> <image_name>:<tag>

  c. to run in interactive mode with bash shell, use -it flag with bash command.  -
    docker run -it -p <host_port>:<container_port> <image_name>:<tag> bash

  Note: to make container small it does not have bash shell, so use sh command.

  d. to run in interactive mode with shell command, use -it flag with shell command.  -
    docker run -it -p <host_port>:<container_port> <image_name>:<tag> sh

8. To execute command in running container, use exec command.  -
    docker exec -it <container_id> bash
    docker exec -it <container_id> sh

    docker exec -it mysql-db sh

    then enter into mysql container and run below command to login into mysql database.  -

    mysql -u root -p

9. to change port of container using environment variable, -e flag is used.  - 
    docker run -p <host_port>:<container_port> -e PORT=<container_port> <image_name>:<tag>

10 to stop container, use stop command.  -
    docker stop <container_id>

11 to remove container, use rm command.  -
    docker rm <container_id>

12 to remove all containers, use rm command with -f flag.  -
    docker rm -f $(docker ps -a -q)
 
13 to remove all stopped containers, use prune command.  -
    docker container prune

14 to remove image, use rmi command.  -
    docker rmi <image_name>:<tag>

15 to share/persist data using volume, use -v flag.  -
    docker run -p <host_port>:<container_port> -v <host_path>:<container_path> <image_name>:<tag>

16 to share/persist data using volume with named volume, use -v flag.  -
    docker run -p <host_port>:<container_port> -v <volume_name>:<container_path> <image_name>:<tag>


ex- docker run -d \
  --name springbootaws-mariadb \
  -p 8081:3306 \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=userdb \
  -v mariadb-data:/var/lib/mysql \
  mariadb:latest


17 we can specify volume in Dockerfile using VOLUME instruction.  -
    VOLUME ["<container_path>"]

  
18 to persist data in host machine can give path of host machine in volume. (bind mount)  -
    docker run -p <host_port>:<container_port> -v <host_path>:<container_path> <image_name>:<tag>

-----------

docker compose is a tool for defining and running multi-container Docker applications. With Compose, you use a YAML file to configure your application’s services. Then, with a single command, you create and start all the services from your configuration.

version: "1.0"
services:
  app:
    image: fullstackdajr:latest
    ports:
      - "8080:8080"
    environment:
      SERVER_PORT: 8080
    volumes:
      - names-volume:/app/src
volumes:
  names-volume:


volume -3 types : name, bind, anonymous volume.

docker compose commands:
1. docker compose up -d  (to start the application in detached mode)
    

### commands 
Common Commands:
  run         Create and run a new container from an image
  exec        Execute a command in a running container
  ps          List containers
  build       Build an image from a Dockerfile
  bake        Build from a file
  pull        Download an image from a registry
  push        Upload an image to a registry
  images      List images
  login       Authenticate to a registry
  logout      Log out from a registry
  search      Search Docker Hub for images
  version     Show the Docker version information
  info        Display system-wide information

Management Commands:
  agent*      Docker AI Agent Runner
  ai*         Docker AI Agent - Ask Gordon
  builder     Manage builds
  buildx*     Docker Buildx
  compose*    Docker Compose
  container   Manage containers
  context     Manage contexts
  debug*      Get a shell into any image or container
  desktop*    Docker Desktop commands
  dhi*        CLI for managing Docker Hardened Images
  extension*  Manages Docker extensions
  image       Manage images
  init*       Creates Docker-related starter files for your project
  manifest    Manage Docker image manifests and manifest lists
  mcp*        Docker MCP Plugin
  model*      Docker Model Runner
  network     Manage networks
  offload*    Docker Offload
  pass*       Docker Pass Secrets Manager Plugin (beta)
  plugin      Manage plugins
  scout*      Docker Scout
  system      Manage Docker
  volume      Manage volumes

Swarm Commands:
  swarm       Manage Swarm

Commands:
  attach      Attach local standard input, output, and error streams to a running container
  commit      Create a new image from a container's changes
  cp          Copy files/folders between a container and the local filesystem
  create      Create a new container
  diff        Inspect changes to files or directories on a container's filesystem
  events      Get real time events from the server
  export      Export a container's filesystem as a tar archive
  history     Show the history of an image
  import      Import the contents from a tarball to create a filesystem image
  inspect     Return low-level information on Docker objects
  kill        Kill one or more running containers
  load        Load an image from a tar archive or STDIN
  logs        Fetch the logs of a container
  pause       Pause all processes within one or more containers
  port        List port mappings or a specific mapping for the container
  rename      Rename a container
  restart     Restart one or more containers
  rm          Remove one or more containers
  rmi         Remove one or more images
  save        Save one or more images to a tar archive (streamed to STDOUT by default)
  start       Start one or more stopped containers
  stats       Display a live stream of container(s) resource usage statistics
  stop        Stop one or more running containers
  tag         Create a tag TARGET_IMAGE that refers to SOURCE_IMAGE
  top         Display the running processes of a container
  unpause     Unpause all processes within one or more containers
  update      Update configuration of one or more containers
  wait        Block until one or more containers stop, then print their exit codes

Global Options:
      --config string      Location of client config files (default
                           "/Users/anjali21i/.docker")
  -c, --context string     Name of the context to use to connect to the
                           daemon (overrides DOCKER_HOST env var and
                           default context set with "docker context use")
  -D, --debug              Enable debug mode
  -H, --host string        Daemon socket to connect to
  -l, --log-level string   Set the logging level ("debug", "info",
                           "warn", "error", "fatal") (default "info")
      --tls                Use TLS; implied by --tlsverify
      --tlscacert string   Trust certs signed only by this CA (default
                           "/Users/anjali21i/.docker/ca.pem")
      --tlscert string     Path to TLS certificate file (default
                           "/Users/anjali21i/.docker/cert.pem")
      --tlskey string      Path to TLS key file (default
                           "/Users/anjali21i/.docker/key.pem")
      --tlsverify          Use TLS and verify the remote
--------