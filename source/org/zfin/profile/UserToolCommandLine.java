package org.zfin.profile;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.profile.service.ProfileService;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command-line administration for user accounts (registered as {@code user-tool} with
 * {@code requiresDatabase=true}, so it runs inside {@code ToolBootstrap.run}'s managed transaction:
 * changes commit on normal return and roll back if this class throws).
 *
 * <pre>{@code
 *   zfin-util user-tool set-password -u staylor 'someTempPass789+'   # set (bcrypt) password
 *   zfin-util user-tool set-role     -u staylor root                 # set access role (root|submit)
 *   zfin-util user-tool show         -u staylor                      # print account info (no secrets)
 * }</pre>
 *
 * The account is identified with {@code -u}/{@code --user} by login name, e-mail address, or
 * ZDB person ID. Passwords are hashed with {@link ProfileService#encodePassword(String)} &mdash; the
 * same BCrypt encoder the web app uses &mdash; so the value stored here is never the plaintext, and
 * the plaintext is echoed nowhere. Modified entities are managed by the current Hibernate session, so
 * the {@code ToolBootstrap} wrapper flushes and commits them; no explicit save is required.
 */
public class UserToolCommandLine {

    private static final Logger LOG = LogManager.getLogger(UserToolCommandLine.class);

    private static final String USAGE = """
        Usage: zfin-util user-tool <subcommand> -u <user> [value]

        Subcommands:
          set-password -u <user> <newPassword>   Set the user's password (stored BCrypt-hashed)
          set-role     -u <user> <role>          Set the user's access role (root | submit)
          show         -u <user>                 Print the user's account info (no secrets)

        <user> may be a login name, e-mail address, or ZDB person ID (given via -u or --user).""";

    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Missing subcommand.\n" + USAGE);
        }

        String subcommand = args[0];
        Args parsed = Args.parse(Arrays.copyOfRange(args, 1, args.length));

        switch (subcommand.toLowerCase()) {
            case "set-password" -> setPassword(parsed);
            case "set-role" -> setRole(parsed);
            case "show", "info" -> show(parsed);
            default -> throw new IllegalArgumentException("Unknown subcommand: " + subcommand + "\n" + USAGE);
        }
    }

    private static void setPassword(Args args) {
        String identifier = args.requireUser();
        String password = args.requireValue("newPassword");
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("Password must not be blank");
        }

        Person person = findPerson(identifier);
        AccountInfo account = requireAccount(person);

        account.setPassword(new ProfileService().encodePassword(password));
        // Invalidate any outstanding reset link now that the password has been changed out from under it.
        account.setPasswordResetKey(null);
        account.setPasswordResetDate(null);

        LOG.info("Set password for user '{}' ({})", account.getLogin(), person.getZdbID());
        System.out.println("Password updated for " + account.getLogin() + " (" + person.getZdbID() + ")");
    }

    private static void setRole(Args args) {
        String identifier = args.requireUser();
        String roleValue = args.requireValue("role");
        AccountInfo.Role role = parseRole(roleValue);

        Person person = findPerson(identifier);
        AccountInfo account = requireAccount(person);

        String previous = account.getRole();
        account.setRole(role.toString());

        LOG.info("Changed role for user '{}' ({}) from '{}' to '{}'",
                account.getLogin(), person.getZdbID(), previous, role);
        System.out.println("Role for " + account.getLogin() + " (" + person.getZdbID() + ") changed from '"
                + previous + "' to '" + role + "'");
    }

    private static void show(Args args) {
        Person person = findPerson(args.requireUser());
        AccountInfo account = requireAccount(person);

        System.out.println("ZDB ID:      " + person.getZdbID());
        System.out.println("Name:        " + person.getFullName());
        System.out.println("Login:       " + account.getLogin());
        System.out.println("Email:       " + person.getEmail());
        System.out.println("Role:        " + account.getRole());
        System.out.println("Curator:     " + account.isCurator());
        System.out.println("Student:     " + account.isStudent());
        // Never print the hash itself; just whether one is set.
        System.out.println("Password:    " + (StringUtils.isNotEmpty(account.getPassword()) ? "set" : "(none)"));
    }

    /**
     * Resolve a user by login name or e-mail (via {@link ProfileService#getPersonByEmailOrLogin}),
     * falling back to a direct ZDB person-ID lookup.
     */
    private static Person findPerson(String identifier) {
        Person person = ProfileService.getPersonByEmailOrLogin(identifier);
        if (person == null && identifier.startsWith("ZDB-PERS-")) {
            person = RepositoryFactory.getProfileRepository().getPerson(identifier);
        }
        if (person == null) {
            throw new IllegalArgumentException("No user found matching: " + identifier);
        }
        return person;
    }

    private static AccountInfo requireAccount(Person person) {
        AccountInfo account = person.getAccountInfo();
        if (account == null) {
            throw new IllegalArgumentException(
                    "User " + person.getZdbID() + " (" + person.getFullName() + ") has no login/account record");
        }
        return account;
    }

    private static AccountInfo.Role parseRole(String roleValue) {
        try {
            return AccountInfo.Role.getRole(roleValue);
        } catch (RuntimeException e) {
            String valid = Arrays.stream(AccountInfo.Role.values())
                    .map(AccountInfo.Role::toString)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid role '" + roleValue + "'. Valid roles: " + valid);
        }
    }

    /**
     * Minimal argument parser: pulls the {@code -u}/{@code --user} option (space- or {@code =}-separated)
     * and treats everything else as a positional value.
     */
    private static final class Args {
        private String user;
        private final List<String> positional = new ArrayList<>();

        static Args parse(String[] rawArgs) {
            Args result = new Args();
            for (int i = 0; i < rawArgs.length; i++) {
                String arg = rawArgs[i];
                if (arg.equals("-u") || arg.equals("--user")) {
                    if (i + 1 >= rawArgs.length) {
                        throw new IllegalArgumentException(arg + " requires a value");
                    }
                    result.user = rawArgs[++i];
                } else if (arg.startsWith("--user=")) {
                    result.user = arg.substring("--user=".length());
                } else {
                    result.positional.add(arg);
                }
            }
            return result;
        }

        String requireUser() {
            if (StringUtils.isBlank(user)) {
                throw new IllegalArgumentException("Missing required option: -u/--user <user>\n" + USAGE);
            }
            return user;
        }

        String requireValue(String label) {
            if (positional.isEmpty()) {
                throw new IllegalArgumentException("Missing required argument: <" + label + ">\n" + USAGE);
            }
            if (positional.size() > 1) {
                throw new IllegalArgumentException("Unexpected extra arguments: "
                        + positional.subList(1, positional.size()) + "\n" + USAGE);
            }
            return positional.get(0);
        }
    }
}
