pipeline {
  agent any
  environment { REGISTRY = credentials('dockerhub-credentials'); TAG = "${env.BUILD_NUMBER}" }
  stages {
    stage('Build and test') { steps { sh 'mvn -B test' } }
    stage('Build images') {
      steps { script { ['config-server','service-discovery','api-gateway','user-service','post-service','feed-service','connection-service','interaction-service','notification-service','product-service','analytics-service'].each { module -> sh "docker build --build-arg MODULE=${module} -t ${REGISTRY_USR}/revconnect-${module}:${TAG} -t ${REGISTRY_USR}/revconnect-${module}:latest ." }; sh "docker build -t ${REGISTRY_USR}/revconnect-frontend:${TAG} -t ${REGISTRY_USR}/revconnect-frontend:latest frontend" } }
    }
    stage('Push images') {
      steps { sh 'echo $REGISTRY_PSW | docker login -u $REGISTRY_USR --password-stdin'; script { ['config-server','service-discovery','api-gateway','user-service','post-service','feed-service','connection-service','interaction-service','notification-service','product-service','analytics-service','frontend'].each { module -> sh "docker push ${REGISTRY_USR}/revconnect-${module}:${TAG}; docker push ${REGISTRY_USR}/revconnect-${module}:latest" } } }
    }
  }
}
