/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
stage('compile') {
    parallel build: {
        node {
            git credentialsId: 'gitlabcredentials', url: 'https://gitlab.com/marioestrella/sethlans.git'
            sh 'mvn compile'
            stash 'everything'
        }
    }
}
stage('test') {
    parallel unitTests: {
        test()
    },
            failFast: false
}
stage('publish') {
    publish()
}

def test() {
    node {
        unstash 'everything'
        sh 'mvn test'
        junit '**/target/surefire-reports/*.xml'
    }
}

def publish() {
    node {
        unstash 'everything'
        sh 'mvn package'
        archiveArtifacts '**/target/binaries/*.jar'
    }
}