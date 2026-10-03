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
				archiveArtifacts artifacts: 'target/app-*.jar', fingerprint: true
			}
		}

		stage("Upload to Nexus") {
			steps {
				withCredentials([
					usernamePassword(
						credentialsId: 'nexus',
						usernameVariable: 'NEXUS_USER',
						passwordVariable: 'NEXUS_PASS'
					)
				]) {
					// URL ends with "/", so curl adds the file name: .../pets/app-<commit>.jar
					// "-f" makes curl fail the build if Nexus returns an error
					sh 'curl -f -u $NEXUS_USER:$NEXUS_PASS --upload-file target/app-*.jar http://nexus:8081/repository/pets/'
				}
			}
		}

		stage("Deploy to dev") {
			steps {
				deploy('vagrant-dev', '2250')
			}
		}
	}
}

// Copies target/app-<commit>.jar to a Vagrant box (as app.jar) and (re)starts the app there.
// credentialsId - Jenkins SSH credential of the box
// port          - SSH port Vagrant forwards to the box (see: vagrant ssh-config <box>)
def deploy(String credentialsId, String port) {
	withEnv([
		"DEPLOY_HOST=host.docker.internal",
		"DEPLOY_PORT=${port}",
		"SSH_OPTS=-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"
	]) {
		withCredentials([
			sshUserPrivateKey(
				credentialsId: credentialsId,
				keyFileVariable: 'SSH_KEY',
				usernameVariable: 'SSH_USER'
			)
		]) {
			// 1. Stop the running app ("|| true" so it doesn't fail when nothing is running yet)
			sh 'ssh $SSH_OPTS -i $SSH_KEY -p $DEPLOY_PORT $SSH_USER@$DEPLOY_HOST "pkill java || true"'

			// 2. Upload the new jar ("-b -" makes sftp fail the build if the upload fails)
			sh 'echo "put target/app-*.jar app.jar" | sftp $SSH_OPTS -i $SSH_KEY -P $DEPLOY_PORT -b - $SSH_USER@$DEPLOY_HOST'

			// 3. Start the app in the background, logs go to app.log
			sh 'ssh $SSH_OPTS -i $SSH_KEY -p $DEPLOY_PORT $SSH_USER@$DEPLOY_HOST "nohup java -jar app.jar > app.log 2>&1 &"'
		}
	}
}
