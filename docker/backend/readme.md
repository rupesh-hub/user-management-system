1. Build Product Service Image
   docker build -t rupesh1997/authorization-server:1.0.0 \
   -t rupesh1997/authorization-server:latest \
   -f ../docker/backend/Dockerfile .

2. Run Product Service Container
   docker run -d -p 8181:8181 --name ums-backend --network=usm-network \
   rupesh1997/ums-backend:1.0.0

3. Docker Network Commands
    - List networks
      docker network ls

    - Create network
      docker network create usm-network --driver bridge

    - Inspect a network
      docker network inspect <network_id_or_name>
      E.g. docker network inspect usm-network

   