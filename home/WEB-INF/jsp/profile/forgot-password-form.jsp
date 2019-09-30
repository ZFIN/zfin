<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<div style="width: 30em; margin: 3em auto; padding-top: 1em;">

    <h2>Reset Password</h2>

    <form action="/action/profile/forgot-password" class="login-box" method="post">
        <h3>Forgot Password</h3>
        <label for="emailOrLogin">Login or Email Address</label>
        <input type="text" name="emailOrLogin" id="emailOrLogin">
        <button type="submit">Submit</button>
    </form>

</div>