pipeline {
	agent any

	stages {
		stage("compile") {
			steps {
				echo "compile"
			}
		}

		stage('SonarQube Cloud') {
			steps {
				withSonarQubeEnv('SonarCloud') {
					mvn sonar:sonar
				}
			}
		}

		stage("run unit tests") {
			steps {
				echo "tests"
			}
		}

		stage("deploy to dev env") {
			steps {
				echo "deploy"
			}
		}
	}
}