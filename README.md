Fork of Graph Drawing library from https://github.com/renatav/GraphDrawing/branches , restructured as Maven multi module project to make it deployable into a repository manager ( archiva / nexus )

command is: 
mvn install deploy -DskipTests -DaltDeploymentRepository=internal::default::https://your-repo-url.com
