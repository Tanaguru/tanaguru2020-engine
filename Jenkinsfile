pipeline {
  agent any
  stages {
	stage('Build') {
	  agent {
	  	docker 'maven'
	  }
	  steps {
	    sh 'mvn clean package -X'
		sh '''
			REST_VERSION=$(mvn -q \
                           -Dexec.executable=echo \
                           -Dexec.args='${project.version}' \
                           --non-recursive \
                           exec:exec)
			echo ${REST_VERSION} > version.txt
		'''
		stash name: 'tanaguru2020-rest', includes: 'tanaguru2020-rest/target/tanaguru-rest-*.tar.gz'
		stash name: 'version', includes: 'version.txt'
	  }
	}

	stage('Build and deploy'){
	  parallel{
	    // Dev
	    stage('Dev') {
		  when {
			branch 'develop'
		  }
		  steps {
			git(url: 'https://github.com/Tanaguru/tanaguru2020-docker', branch: 'master', credentialsId: 'github-rcharre')
			unstash 'tanaguru2020-rest'
			unstash 'version'
			sh '''
				REST_VERSION=$(cat version.txt)
				mv tanaguru2020-rest-*.tar.gz ./tanaguru2020-rest/image/tanaguru2020-rest-${REST_VERSION}.tar.gz
				docker build -t tanaguru2020-rest:dev --build-arg TANAGURU_REST_ARCHIVE_PATH=tanaguru2020-rest-${REST_VERSION}.tar.gz --build-arg FIREFOX_ARCHIVE_PATH=http://download-origin.cdn.mozilla.net/pub/firefox/releases/69.0/linux-x86_64/en-US/firefox-69.0.tar.bz2 --build-arg GECKODRIVER_ARCHIVE_PATH=https://github.com/mozilla/geckodriver/releases/download/v0.21.0/geckodriver-v0.21.0-linux64.tar.gz ./tanaguru2020-rest/image/
			'''
			sh 'docker image prune -f'
			sh 'docker stop tanaguru2020-rest-dev || true'
			sh 'docker start tanaguru2020-rest-dev || true'
		  }
	    }

		// Master
		stage('Master') {
		  when {
			branch 'master'
		  }
		  steps {
			git(url: 'https://github.com/Tanaguru/tanaguru2020-docker', branch: 'master', credentialsId: 'github-rcharre')
			unstash 'tanaguru2020-rest'
			unstash 'version'
			sh '''
				REST_VERSION=$(cat version.txt)
				mv tanaguru2020-rest.tar.gz ./tanaguru2020-rest/image/tanaguru2020-rest-${WEBAPP_VERSION}.tar.gz
				docker build -t tanaguru2020-rest:latest --build-arg TANAGURU_REST_ARCHIVE_PATH=tanaguru2020-rest-${REST_VERSION}.tar.gz --build-arg FIREFOX_ARCHIVE_PATH=http://download-origin.cdn.mozilla.net/pub/firefox/releases/69.0/linux-x86_64/en-US/firefox-69.0.tar.bz2 --build-arg GECKODRIVER_ARCHIVE_PATH=https://github.com/mozilla/geckodriver/releases/download/v0.21.0/geckodriver-v0.21.0-linux64.tar.gz ./tanaguru2020-rest/image/
			'''
			sh 'docker image prune -f'
		  }
		}
	  }
	}
  }
}
