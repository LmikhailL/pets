pipeline {
	agent any

	stages {
		stage("compile") {
			steps {
				echo "compile"
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