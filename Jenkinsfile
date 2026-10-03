pipeline {
	agent any

	tools {
		maven 'mvn3.9'
		jdk 'jdk25'
	}

	stages {
		stage("Compile") {
			steps {
				sh 'mvn -B -ntp -q compile'
			}
		}

		stage("Spotless") {
			steps {
				sh 'mvn -B -ntp -q spotless:check'
			}
		}

		stage('SonarQube Cloud') {
			steps {
				withSonarQubeEnv('SonarCloud') {
					sh 'mvn -B -ntp -q sonar:sonar'
				}
			}
		}

		stage("Unit Tests") {
			steps {
				sh 'mvn -B -ntp -q test'
			}
		}

		stage("Integration Tests") {
			steps {
				sh 'mvn -B -ntp -q verify -DskipUTs=true'
			}
		}

		stage("Deploy") {
			steps {
				echo "deploy"
			}
		}
	}
}
