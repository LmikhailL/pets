pipeline {
	agent any

	tools {
		maven 'mvn3.9'
		jdk 'jdk25'
	}

	stages {
		stage("Compile") {
			steps {
				sh 'mvn -B -ntp -q clean compile'
			}
		}

		stage("Spotless") {
			steps {
				sh 'mvn -B -ntp -q spotless:check'
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

		stage('SonarCloud') {
			steps {
				withSonarQubeEnv('SonarCloud') {
					sh 'mvn -B -ntp -q sonar:sonar'
				}
			}
		}

		stage("Build") {
			steps {
				sh 'mvn -B -ntp -q package -DskipUTs=true -DskipITs=true'
				archiveArtifacts artifacts: 'target/app.jar', fingerprint: true
			}
		}

		stage("Deploy") {
			environment {
				DEPLOY_HOST = 'host.docker.internal'
				DEPLOY_PORT = '2250'
				SSH_OPTS = '-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null'
			}
			steps {
				withCredentials([
					sshUserPrivateKey(
						credentialsId: 'vagrant-dev',
						keyFileVariable: 'SSH_KEY',
						usernameVariable: 'SSH_USER'
					)
				]) {
					// 1. Stop the running app
					sh 'ssh $SSH_OPTS -i $SSH_KEY -p $DEPLOY_PORT $SSH_USER@$DEPLOY_HOST "sudo systemctl stop pets"'

					// 2. Upload the new jar ("-b -" makes sftp fail the build if the upload fails)
					sh 'echo "put target/app.jar /opt/pets/app.jar" | sftp $SSH_OPTS -i $SSH_KEY -P $DEPLOY_PORT -b - $SSH_USER@$DEPLOY_HOST'

					// 3. Start the app again
					sh 'ssh $SSH_OPTS -i $SSH_KEY -p $DEPLOY_PORT $SSH_USER@$DEPLOY_HOST "sudo systemctl start pets"'
				}
			}
		}
	}
}
