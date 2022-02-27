package com.lesfurets.jenkins.unit

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class PipelineTestHelperTest {
    private PipelineTestHelper helper

    @Before
    void setUp() throws Exception {
        helper = new PipelineTestHelper()
    }

    @Test
    void testRegisterAllowedMethodWithoutArgs() {
        // given:
        def closure = { println 'withoutArgs' }
        helper.registerAllowedMethod('withoutArgs', closure)

        // when:
        Map.Entry<MethodSignature, Closure> allowedMethodEntry = helper.getAllowedMethodEntry('withoutArgs')

        // then:
        Assertions.assertThat(allowedMethodEntry.getKey().getArgs().size()).isEqualTo(0)
        Assertions.assertThat(allowedMethodEntry.getValue()).isEqualTo(closure)
    }

    @Test
    void testRegisterAllowedMethodEmptyArgs() {
        // given:
        def closure = { println 'emptyArgsList' }
        helper.registerAllowedMethod('emptyArgsList', closure)

        // when:
        Map.Entry<MethodSignature, Closure> allowedMethodEntry = helper.getAllowedMethodEntry('emptyArgsList')

        // then:
        Assertions.assertThat(allowedMethodEntry.getKey().getArgs().size()).isEqualTo(0)
        Assertions.assertThat(allowedMethodEntry.getValue()).isEqualTo(closure)
    }

    @Test
    void readFile() {
        // given:
        helper.addFileExistsMock('test', true)

        // when:
        def result = helper.fileExists('test')

        // then:
        Assertions.assertThat(result).isTrue()
    }

    @Test
    void readFileNotMocked() {
        // given:

        // when:
        def result = helper.fileExists('test')

        // then:
        Assertions.assertThat(result).isFalse()
    }

    @Test
    void readFileWithMap() {
        // given:
        helper.addReadFileMock('test', 'contents')

        // when:
        def output = helper.readFile(file: 'test')

        // then:
        Assertions.assertThat(output).isEqualTo('contents')
    }

    @Test
    void readFileWithNoMockOutput() {
        // given:

        // when:
        def output = helper.readFile('test')

        // then:
        Assertions.assertThat(output).isEqualTo('')
    }

    @Test
    void runSh() {
        // given:
        helper.addShMock('pwd', '/foo/bar', 0)

        // when:
        def output = helper.runSh('pwd')

        // then:
        Assertions.assertThat(output).isNull()
    }

    @Test
    void runShWithScriptFailure() {
        // given:
        helper.addShMock('evil', '/foo/bar', 666)
        Exception caught = null

        // when:
        try {
            helper.runSh('evil')
        } catch (e) {
            caught = e
        }

        // then: Exception raised
        Assertions.assertThat(caught).isNotNull()
        Assertions.assertThat(caught.message).isEqualTo('script returned exit code 666')
    }

    @Test
    void runShWithStdout() {
        // given:
        helper.addShMock('pwd', '/foo/bar', 0)

        // when:
        def output = helper.runSh(returnStdout: true, script: 'pwd')

        // then:
        Assertions.assertThat(output).isEqualTo('/foo/bar')
    }

    @Test(expected = Exception)
    void runShWithStdoutFailure() {
        // given:
        helper.addShMock('pwd', '/foo/bar', 1)

        // when:
        helper.runSh(returnStdout: true, script: 'pwd')

        // then: Exception raised
    }

    @Test
    void runShWithReturnCode() {
        // given:
        helper.addShMock('pwd', '/foo/bar', 0)

        // when:
        def output = helper.runSh(returnStatus: true, script: 'pwd')

        // then:
        Assertions.assertThat(output).isEqualTo(0)
    }

    @Test
    void runShWithNonZeroReturnCode() {
        // given:
        helper.addShMock('evil', '/foo/bar', 666)

        // when:
        def output = helper.runSh(returnStatus: true, script: 'evil')

        // then:
        Assertions.assertThat(output).isEqualTo(666)
    }

    @Test
    void runShWithCallback() {
        // given:
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar', exitValue: 0]
        }

        // when:
        def output = helper.runSh('pwd')

        // then:
        Assertions.assertThat(output).isNull()
    }

    @Test(expected = Exception)
    void runShWithCallbackScriptFailure() {
        // given:
        helper.addShMock('evil') { script ->
            return [stdout: '/foo/bar', exitValue: 666]
        }

        // when:
        helper.runSh('evil')

        // then: Exception raised
    }

    @Test
    void runShWithCallbackStdout() {
        // given:
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar', exitValue: 0]
        }

        // when:
        def output = helper.runSh(returnStdout: true, script: 'pwd')

        // then:
        Assertions.assertThat(output).isEqualTo('/foo/bar')
    }

    @Test
    void runShWithCallbackReturnCode() {
        // given:
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar', exitValue: 0]
        }

        // when:
        def output = helper.runSh(returnStatus: true, script: 'pwd')

        // then:
        Assertions.assertThat(output).isEqualTo(0)
    }

    @Test
    void runShWithCallbackNonZeroReturnCode() {
        // given:
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar', exitValue: 666]
        }

        // when:
        def output = helper.runSh(returnStatus: true, script: 'pwd')

        // then:
        Assertions.assertThat(output).isEqualTo(666)
    }

    @Test(expected = IllegalArgumentException)
    void runShWithCallbackOutputNotMap() {
        // given:
        helper.addShMock('pwd') { script ->
            return 'invalid'
        }

        // when:
        helper.runSh(returnStatus: true, script: 'pwd')

        // then: Exception raised
    }

    @Test(expected = IllegalArgumentException)
    void runShWithCallbackNoStdoutKey() {
        // given:
        helper.addShMock('pwd') { script ->
            return [exitValue: 666]
        }

        // when:
        helper.runSh(returnStatus: true, script: 'pwd')

        // then: Exception raised
    }

    @Test(expected = IllegalArgumentException)
    void runShWithCallbackNoExitValueKey() {
        // given:
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar']
        }

        // when:
        helper.runSh(returnStatus: true, script: 'pwd')

        // then: Exception raised
    }

    @Test()
    void runShWithoutMockOutput() {
        // given:

        // when:
        def output = helper.runSh('unregistered-mock-output')

        // then:
        Assertions.assertThat(output).isNull()
    }

    @Test()
    void runShWithoutMockOutputAndReturnStatus() {
        // given:

        // when:
        def output = helper.runSh(returnStatus: true, script: 'unregistered-mock-output')

        // then:
        Assertions.assertThat(output).isEqualTo(0)
    }

    @Test()
    void runShWithoutMockOutputAndReturnStdout() {
        // given:

        // when:
        def output = helper.runSh(returnStdout: true, script: 'unregistered-mock-output')

        // then:
        Assertions.assertThat(output).isEqualTo('')
    }

    @Test(expected = IllegalArgumentException)
    void runShWithBothStatusAndStdout() {
        // given:

        // when:
        helper.runSh(returnStatus: true, returnStdout: true, script: 'invalid')

        // then: Exception raised
    }

}
