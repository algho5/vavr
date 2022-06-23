library 'jenkins-shared-library@main'

pipeline {
    agent {
        docker {
            image 'registry.jeroensteenbeeke.nl/maven:latest'
            label 'docker'
        }
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        disableConcurrentBuilds()
    }

    environment {
        MAVEN_DEPLOY_USER = credentials('MAVEN_DEPLOY_USER')
        MAVEN_DEPLOY_PASSWORD = credentials('MAVEN_DEPLOY_PASSWORD')
    }

    stages {
        stage('Cleanup') {
            steps {
                sh "for f in \$(find * | grep jacoco.exec\$); do rm \$f; done"
            }
        }
        stage('Maven') {
            steps {
                sh 'mvn -B -U clean verify package'
            }
        }
        stage('Coverage') {
            when {
                expression {
                    currentBuild.result == 'SUCCESS' || currentBuild.result == null
                }
            }

            steps {
                jacoco buildOverBuild: true, changeBuildStatus: true, exclusionPattern: '**/*Test*.class,**/Equivalence.class'
            }
        }
        stage('Deploy') {
            when {
                expression {
                    currentBuild.result == 'SUCCESS' || currentBuild.result == null
                }
            }

            steps {
                mavenDeploy deployUser: env.MAVEN_DEPLOY_USER,
                        deployPassword: env.MAVEN_DEPLOY_PASSWORD
            }
        }
    }

    post {
        always {
            script {
                if (currentBuild.result == null) {
                    currentBuild.result = 'SUCCESS'
                }
            }
            step([$class                  : 'Mailer',
                  notifyEveryUnstableBuild: true,
                  sendToIndividuals       : true,
                  recipients              : 'j.steenbeeke@gmail.com'
            ])
            recordIssues enabledForFailure: true, tools: [mavenConsole(), java(), javaDoc()]
            junit allowEmptyResults: true, testResults: '**/target/test-reports/*.xml'
            recordIssues enabledForFailure: true, tool: checkStyle(pattern: '**/target/checkstyle-result.xml')
            recordIssues enabledForFailure: true, tool: spotBugs()
        }
    }

}
