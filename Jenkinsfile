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
    node {
        deleteDir()
        git url: 'https://gitlab.com/marioestrella/sethlans.git'
        sh 'mvn clean compile'
    }
}
stage('unitests') {
    parallel linux: {
        node('linux') {
            deleteDir()
            git url: 'https://gitlab.com/marioestrella/sethlans.git'
            sh 'mvn clean test'
            junit '**/target/surefire-reports/*.xml'
        }
    }, windows: {
        node('windows') {
            deleteDir()
            git url: 'https://gitlab.com/marioestrella/sethlans.git'
            sh 'mvn clean test'
            junit '**/target/surefire-reports/*.xml'
        }
    }, mac: {
        node('mac') {
            deleteDir()
            git url: 'https://gitlab.com/marioestrella/sethlans.git'
            sh 'mvn clean test'
            junit '**/target/surefire-reports/*.xml'
        }
    }, failFast: false
}
stage('publish') {
    node('package') {
        deleteDir()
        git url: 'https://gitlab.com/marioestrella/sethlans.git'
        sh 'mvn clean package'
        archiveArtifacts artifacts: '**/target/binaries/*.jar, **/target/binaries/*.exe, **/target/binaries/*.dmg', fingerprint: true
    }
}