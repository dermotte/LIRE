Using openCV with LIRe
======================

In order to use openCV library with LIRe, the java.library.path system property is needed to be specified as following (for IntelliJ IDEA):
In Tools -> Run/Debug Configuration -> Application -> VM options, enter:
-Djava.library.path=path/to/dll
e.g.: -Djava.library.path="lib\opencv"

Current version: openCV 2.4.9 (x64)