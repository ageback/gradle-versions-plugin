package com.github.benmanes.gradle.versions

import java.io.File
import java.nio.file.Files
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

final class ConstraintsSpec extends Specification {
  private File testProjectDir = Files.createTempDirectory('test').toFile()

  private File buildFile
  private String mavenRepoUrl

  def 'setup'() {
    mavenRepoUrl = getClass().getResource('/maven/').toURI()
  }

  def "Show updates for an api dependency constraint"() {
    given:
    buildFile = new File(testProjectDir, 'build.gradle')
    buildFile <<
      """
        plugins {
          id 'java-library'
          id 'com.github.ben-manes.versions'
        }

        tasks.dependencyUpdates {
          checkConstraints = true
        }

        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }

        dependencies {
          constraints {
            api 'com.google.inject:guice:2.0'
          }
        }
      """.stripIndent()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir)
      .withArguments('dependencyUpdates')
      .withPluginClasspath()
      .build()

    then:
    result.output.contains('com.google.inject:guice [2.0 -> 3.1]')
    result.task(':dependencyUpdates').outcome == SUCCESS
  }

  def "Does not override explicit dependency with constraint"() {
    given:
    buildFile = new File(testProjectDir, 'build.gradle')
    buildFile <<
      """
        plugins {
          id 'java-library'
          id 'com.github.ben-manes.versions'
        }

        tasks.dependencyUpdates {
          checkConstraints = true
        }

        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }

        dependencies {
          api 'com.google.inject:guice:3.0'
          constraints {
            api 'com.google.inject:guice:2.0'
          }
        }
      """.stripIndent()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir)
      .withArguments('dependencyUpdates')
      .withPluginClasspath()
      .build()

    then:
    result.output.contains('com.google.inject:guice [3.0 -> 3.1]')
    result.task(':dependencyUpdates').outcome == SUCCESS
  }

  def "Does not show updates for an api dependency constraint when disabled"() {
    given:
    def mavenRepoUrl = getClass().getResource('/maven/').toURI()
    buildFile = new File(testProjectDir, 'build.gradle')
    buildFile <<
      """
        plugins {
          id 'java-library'
          id 'com.github.ben-manes.versions'
        }

        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }

        dependencies {
          constraints {
            api 'com.google.inject:guice:2.0'
          }
        }
      """.stripIndent()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir)
      .withArguments('dependencyUpdates')
      .withPluginClasspath()
      .build()

    then:
    result.output.contains('No dependencies found.')
    result.task(':dependencyUpdates').outcome == SUCCESS
  }
}
