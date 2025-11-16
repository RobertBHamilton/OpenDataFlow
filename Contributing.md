The big picture.

1. To make valuable contributions, first task is to become aware of the contract between the four layers of this application:  Core,Service, and Utility, and a collection of shell scripts which comprise the fourth.

Core provides all core functionality. Access to databases and resources, and methods to retrieve them. Core methods which interface with database always emit ResultSet. 
Service is the layer which stands between core and all users and applications.  Service functions generally consume ResultSet's and  emit json.
Applications will hit service layer only.  The idea is that applications only ever handle json outputs. They never need to use jdbc methods.

Utility is a particular  application which provides command line functionality, but provides convenience methods for other loosely coupled consumers.
Shell scripts will only ever call upon classes in utility.

Core and the interface between core and service are private and subject to change at any time. No application should hit core directly. If there is a core function which you need, then write a service layer function which does it. 

The public layer and the  interface between service and utility is public. Once we reach first release this interface is locked. 

Same is true for utility layer. The class named "util" in this layer is the main for the executable jar and all dataflow functions are exposed in that class.

That design is intended to simplify the use of DataFlow on the command line.  Other than utilitys scripts, ETL scripts have no need to use this utility, but when manual intervention is required, the utility.util class is intended to give the devops user the keys to the kingdom.

As a digression, it is quite possible to write shell scripts or java applications which consume functions from any of the layers.
For example if  one wanted to execute an SQL statement he could in principle 1)run DataProvider.runSQL in the core and grab a ResultSet 2) run DataFlow.runSQL in the service layer and grap a json result 3) execute  RunSQL.run in the utility layer, or 4) use the util class with an 'sql' command line argument, or even 5) use the utility.sh script with an argument of 'sql'  and 'select * ...rest of select statement " as second arg. 

It is matter of discipline which method we use. The service layer and utility layer should be the most stable, so the use is expected to use them. 

If you see where this is being violated, then you can make a very valueable contribution right away by fixing it!

2. You can see from the code that my preference is 4 space indents. And we use pascall style braces. Sorry if that makes you uncomfortable. But old-school programmers like me still use line oriented text editors and real estate is is of upmost importance. Just indulge me ok?

3. We are weak on junit testing. If you like to write junit tests then you will be a valued contributer. We need full code coverage, but we value quality. Ideally unit tests should be well documented, describing the purpose of the test, and what we might learn if it fails/passes.

4. Pull requests.  Just do a standard pull request. It's a small project so I am the only approver. Trust me, I'm not in the not-invented-here camp. Anything that improves the product is totally welcome.
