
$java_files = (gci -Recurse -Include *.java src).FullName

javac -Xdiags:verbose -Xlint:unchecked -d out $java_files
#java -cp out Server
java -cp out testes.TesteA