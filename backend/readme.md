2️⃣ CustomProductionAuthenticationProvider
Purpose: Authenticate users at login.
It is called only during login (when the user submits username/password).
It says: “I will check the credentials against my database (or any source). If correct, I will return a valid Authentication object.”
Step by step flow:
User submits /login with username/password.
Spring Security uses UsernamePasswordAuthenticationFilter.
This filter calls your CustomProductionAuthenticationProvider.
Inside authenticate():
Loads user from DB (UserDetailsService)
Checks if password matches (PasswordEncoder.matches)
If yes → returns UsernamePasswordAuthenticationToken with user authorities
Spring Security stores this Authentication in:
SecurityContextHolder
And by default, also in HttpSession (so you don’t have to log in again)
✅ Layman version: This is the “credential checker” at login.

3️⃣ CustomSessionValidationFilter
Purpose: Validate session on subsequent requests.
This filter runs on every request to protected resources.
It says: “The user already has a session ID (JSESSIONID cookie). Let’s check if it’s valid and restore the user’s authentication.”
Step by step flow:
User sends a request to /users or any protected endpoint.
Browser automatically sends JSESSIONID cookie.
Your filter runs:
Checks if the session ID is valid (request.isRequestedSessionIdValid() or your custom logic)
Optionally, loads the user from DB if you want to verify the session manually
Puts a valid Authentication in SecurityContextHolder
Spring Security sees the context is authenticated → allows access.
✅ Layman version: This is the “badge checker” for every protected resource after login.

4️⃣ How They Work Together
Step -->	Who handles it? --> 	What happens?
User logs in --->	CustomProductionAuthenticationProvider --->	Checks username/password, stores Authentication in session
User makes request to /users --->	CustomSessionValidationFilter --->	Reads session ID, restores Authentication to SecurityContextHolder
Spring Security  --->  Default filters (FilterSecurityInterceptor) --->  Checks if Authentication exists and has required roles → allows or denies access


Q. How Spring Security handles Authorization ?
After the SecurityContextHolder has a valid Authentication:
The FilterSecurityInterceptor runs last in the Spring Security filter chain.
This is the component that checks if the current user has enough privileges to access the requested URL.
It uses two things to decide:
SecurityMetadataSource → maps URLs or methods to required roles
Example: /admin/** → ROLE_ADMIN
Configured via .authorizeHttpRequests() in your HttpSecurity config.
AccessDecisionManager → checks if the current user (from SecurityContextHolder) has the required roles.
If the user does not have the required role → Spring throws AccessDeniedException → handled by AccessDeniedHandler.


Spring Security will automatically get the Authentication from SecurityContextHolder, check getAuthorities() and decide if access is allowed.
We don’t have to manually get authorities or check roles.

