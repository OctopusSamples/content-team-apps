This project exists to provide a HTTP endpoint over the Azure service bus.

Unfortunately the Azure dependencies just don't work with GraalVM native compilation. There are
too many instances of things like static SecureRandoms in classes and a whole slew of 
Kerberos dependencies that I wasn't confident I could sort out.

The solution is to move the service bus dependencies into their own service and build it as 
a plain Java application. Messages sent to the service bus are likely to be called async anyway,
so the startup time is not of great concern.

So interacting with the Azure service bus is just like any other JSON API REST service now.