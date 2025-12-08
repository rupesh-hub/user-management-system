1. Build docker image
   docker build \
   -t rupesh1997/ums-frontend:1.0.0 \
   --build-arg CONFIGURATION=docker \
   -f ../docker/frontend/Dockerfile .

   ---
   docker build \
   -t rupesh1997/ums-frontend:1.0.0 \
   -f ../docker/frontend/Dockerfile .

   ---
   docker build \
   -t rupesh1997/ums-frontend:1.0.0 \
   --build-arg CONFIGURATION=production \
   -f ../docker/frontend/Dockerfile .

2. Run image
   docker run -d \
   --name ums-frontend \
   --network usm-network \
   -p 8080:8080 \
   rupesh1997/ums-frontend:1.0.0

   OR
   docker run -d \
   --name ums-frontend \
   -p 8080:8080 \
   rupesh1997/ums-frontend:1.0.0


# docker kill  ums-frontend && docker rm  ums-frontend
# docker system prune -f

