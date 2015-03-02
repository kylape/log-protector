JBoss LogManager Protector

Simple web application (servlet) to utilize the protection feature of JBoss
LogManager.  Go to `http://<host>:<port>/log-protector` to see the current
state and a form button to toggle current protection state.  

Note that if you redeploy the application while the LogManager is in the
protected state, you will no longer be able to unprotect the LogManager until a
server restart.
