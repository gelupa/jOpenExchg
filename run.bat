set CLASSPATH=.\bin
set GCDEBUG=

rem set GCDEBUG=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps

java -Xmx800M -Xms800M -Xmn50M -Xss32K -XX:SurvivorRatio=10 %GCDEBUG% -cp %CLASSPATH% org.jopenexchg.MatcherDemo

pause

