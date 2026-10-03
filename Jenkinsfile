pipeline {
	agent any

	tools {
		maven 'mvn3.9'
		jdk 'jdk25'
	}

	stages {
		stage("compile") {
			steps {
				echo "compile"
			}
		}

		stage('SonarQube Cloud') {
			steps {
				withSonarQubeEnv('SonarCloud') {
					sh 'mvn sonar:sonar'
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