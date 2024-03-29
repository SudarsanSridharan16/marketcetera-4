package org.marketcetera.ors.security;

import static org.marketcetera.ors.security.Messages.*;

import java.io.Console;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.cli.*;
import org.apache.commons.lang.SystemUtils;
import org.marketcetera.core.ApplicationContainer;
import org.marketcetera.ors.dao.UserService;
import org.marketcetera.persist.PersistenceException;
import org.marketcetera.util.except.I18NException;
import org.marketcetera.util.log.I18NBoundMessage1P;
import org.marketcetera.util.log.I18NMessage1P;
import org.marketcetera.util.misc.ClassVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;


/* $License$ */
/**
 * The CLI to manage users and password on ORS.
 * Invoke {@link #parseAndRun(String[])} to run a CLI command. This method
 * can be invoked multiple times to invoke several commands.
 * The CLI instance can eventually be destroyed by invoking {@link #close()} 
 *
 * @author anshul@marketcetera.com
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 */
@ClassVersion("$Id: ORSAdminCLI.java 16468 2014-05-12 00:36:56Z colin $")
public class ORSAdminCLI
        implements Lifecycle
{
    /**
     * Get the out value.
     *
     * @return a <code>PrintStream</code> value
     */
    public PrintStream getOut()
    {
        return out;
    }
    /**
     * Sets the out value.
     *
     * @param inOut a <code>PrintStream</code> value
     */
    public void setOut(PrintStream inOut)
    {
        out = inOut;
    }
    /**
     * Get the err value.
     *
     * @return a <code>PrintStream</code> value
     */
    public PrintStream getErr()
    {
        return err;
    }
    /**
     * Sets the err value.
     *
     * @param inErr a <code>PrintStream</code> value
     */
    public void setErr(PrintStream inErr)
    {
        err = inErr;
    }
    /**
     * Get the console value.
     *
     * @return a <code>Console</code> value
     */
    public Console getConsole()
    {
        return console;
    }
    /**
     * Sets the console value.
     *
     * @param inConsole a <code>Console</code> value
     */
    public void setConsole(Console inConsole)
    {
        console = inConsole;
    }
    /**
     * Parses and runs the supplied command. This method
     * can be invoked multiple times.
     *
     * @param args the arguments per the usage of the CLI
     * @throws Exception if an error occurs running the command 
     */
    public void parseAndRun(String... args)
            throws Exception
    {
        try {
            execute(new GnuParser().parse(options(),
                                          args));
        } catch (Exception e) {
            printError(e.getLocalizedMessage());
            printUsage();
            throw e;
        }
    }
    /**
     * Get the userService value.
     *
     * @return a <code>UserService</code> value
     */
    public UserService getUserService()
    {
        return userService;
    }
    /**
     * Sets the userService value.
     *
     * @param inUserService a <code>UserService</code> value
     */
    @Autowired
    public void setUserService(UserService inUserService)
    {
        userService = inUserService;
    }
    /* (non-Javadoc)
     * @see org.springframework.context.Lifecycle#isRunning()
     */
    @Override
    public boolean isRunning()
    {
        return running.get();
    }
    /* (non-Javadoc)
     * @see org.springframework.context.Lifecycle#start()
     */
    @Override
    public void start()
    {
        try {
            parseAndRun(ApplicationContainer.getInstance().getArguments());
            running.set(true);
        } catch (Exception alreadyhandled) {
        } finally {
            ApplicationContainer.stopInstanceWaiting();
        }
    }
    /* (non-Javadoc)
     * @see org.springframework.context.Lifecycle#stop()
     */
    @Override
    public void stop()
    {
        try {
            
        } finally {
            running.set(false);
        }
    }
    /**
     * Reads the password from the console if one is available.
     *
     * @param message the password prompt to display to the user
     * 
     * @return the password, null, if no console is available or
     * if end of stream is reached.
     */
    protected char[] readPasswordFromConsole(String message)
    {
        if(console == null) {
            return null;
        } else {
            return console.readPassword(message);
        }
    }
    /**
     * Executes the command line based on the supplied options
     *
     * @param commandLine the parsed command line
     *
     * @throws I18NException if there were errors in the command line options
     * or if there were errors executing commands.
     */
    private void execute(CommandLine commandLine)
    {
        String userName = commandLine.getOptionValue(OPT_CURRENT_USER);
        String password = commandLine.getOptionValue(OPT_CURRENT_PASSWORD);
        String opUser = commandLine.getOptionValue(OPT_OPERATED_USER);
        String opPass = commandLine.getOptionValue(OPT_OPERATED_PASSWORD);
        Boolean opSuperuser = null;
        if(commandLine.hasOption(OPT_OPERATED_SUPERUSER)) {
            opSuperuser = commandLine.getOptionValue(OPT_OPERATED_SUPERUSER).trim().equals(OPT_YES);
        }
        Boolean opActive = null;
        if(commandLine.hasOption(OPT_OPERATED_ACTIVE)) {
            opActive = commandLine.getOptionValue(OPT_OPERATED_ACTIVE).trim().equals(OPT_YES);
        }
        if(commandLine.hasOption(CMD_ADD_USER)) {
            if(!commandLine.hasOption(OPT_OPERATED_USER)) {
                throw new I18NException(new I18NBoundMessage1P(CLI_ERR_OPTION_MISSING,
                                                               OPT_OPERATED_USER));
            }
            if(!commandLine.hasOption(OPT_OPERATED_PASSWORD)) {
                opPass = getOptionFromConsole(opUser,
                                              opPass,
                                              OPT_OPERATED_PASSWORD,
                                              CLI_PROMPT_PASSWORD);
            }
            authorize(Authorization.ADD_USER,
                      userName,
                      password);
            addUser(opUser,
                    opPass,
                    opSuperuser);
        } else if(commandLine.hasOption(CMD_DELETE_USER)) {
            if(!commandLine.hasOption(OPT_OPERATED_USER)) {
                throw new I18NException(new I18NBoundMessage1P(CLI_ERR_OPTION_MISSING,
                                                               OPT_OPERATED_USER));
            }
            authorize(Authorization.DELETE_USER,
                      userName,
                      password);
            deleteUser(opUser);
        } else if(commandLine.hasOption(CMD_RESTORE_USER)) {
            if(!commandLine.hasOption(OPT_OPERATED_USER)) {
                throw new I18NException(new I18NBoundMessage1P(CLI_ERR_OPTION_MISSING,
                                                               OPT_OPERATED_USER));
            }
            authorize(Authorization.RESTORE_USER,
                      userName,
                      password);
            restoreUser(opUser);
        } else if (commandLine.hasOption(CMD_LIST_USERS)) {
            authorize(Authorization.LIST_USERS,
                      userName,
                      password);
            listUsers(opUser,
                      opActive);
        } else if (commandLine.hasOption(CMD_CHANGE_PASS)) {
            //The order of these statements is important as it
            //determines the order in which the user is prompted
            //First we want to get the login password
            //and then we want to prompt for the new password
            //if one wasn't supplied on the command line
            //Get the login password
            if(!commandLine.hasOption(OPT_CURRENT_PASSWORD)) {
                password = getOptionFromConsole(userName, password,
                        OPT_CURRENT_PASSWORD, CLI_PROMPT_PASSWORD);
            }
            //Get the new password
            if(!commandLine.hasOption(OPT_OPERATED_PASSWORD)) {
                String curUser = opUser == null? userName: opUser;
                opPass = getOptionFromConsole(curUser, opPass,
                        OPT_OPERATED_PASSWORD, CLI_PROMPT_NEW_PASSWORD);
            }
            //Only authorize if changing password for a different user
            if(commandLine.hasOption(OPT_OPERATED_USER) &&
               !userName.equals(opUser)) {
                authorize(Authorization.CHANGE_PASSWORD,userName,password);
            }
            changePassword(userName, opUser, password, opPass);
        } else if (commandLine.hasOption(CMD_CHANGE_SUPERUSER)) {
            if(!commandLine.hasOption(OPT_OPERATED_USER)) {
                throw new I18NException(new I18NBoundMessage1P(
                        CLI_ERR_OPTION_MISSING, OPT_OPERATED_USER));
            }
            if (opSuperuser==null) {
                throw new I18NException(new I18NBoundMessage1P(
                        CLI_ERR_OPTION_MISSING, OPT_OPERATED_SUPERUSER));
            }
            authorize(Authorization.CHANGE_SUPERUSER,userName,password);
            changeSuperuser(opUser, opSuperuser);
        } else {
            //A MissingOptionException would have been already thrown
            throw new IllegalStateException();
        }
    }
    private void authorize(Authorization auth,
                           String userName,
                           String password) throws I18NException {
        password = getOptionFromConsole(userName, password,
                OPT_CURRENT_PASSWORD, CLI_PROMPT_PASSWORD);
        SimpleUser simpleUser = userService.findByName(userName);
        if(simpleUser == null) {
            throw new I18NException(CLI_ERR_INVALID_LOGIN);
        }
        auth.authorize(simpleUser, password);
    }

    private String getOptionFromConsole(String userName,
                                        String password,
                                        String optName,
                                        I18NMessage1P prompt)
            throws I18NException {
        if(password == null) {
            char[] p = readPasswordFromConsole(
                    prompt.getText(userName));
            if(p == null) {
                throw new I18NException(new I18NBoundMessage1P(
                        CLI_ERR_OPTION_MISSING, optName));
            }
            password = new String(p);
            Arrays.fill(p,'0');
        }
        return password;
    }

    /**
     * Fetches an active user with the given name.
     *
     * @param user the user name
     * 
     * @throws I18NException if there were errors fetching the user.
     */

    private SimpleUser fetchUser(String user)
        throws I18NException
    {
        SimpleUser u = userService.findByName(user);
        if(u == null) {
            throw new I18NException(CLI_ERR_INVALID_LOGIN);
        }
        if(!u.isActive()) {
            throw new I18NException(CLI_ERR_INACTIVE_USER);
        }
        return u;
    }

    /**
     * Changes the user password.
     *
     * @param userName the user name of the user running the command
     * @param opUser the name of the user whose password needs to be reset.
     * Can be null. If null, the password of the user running the command
     * is changed
     * @param password the password supplied by the user running the command
     * @param opPass the new password value.
     * 
     * @throws I18NException if there were errors reseting the password.
     */
    private void changePassword(String userName,
                                String opUser,
                                String password,
                                String opPass)
        throws I18NException {
        SimpleUser u = null;
        if(opUser != null && !opUser.equals(userName)) {
            u = fetchUser(opUser);
            u.resetUserPassword(opPass);
            //go through set name to reset the password as we do not have
            //the original password
        } else {
            u = fetchUser(userName);
            u.changePassword(password.toCharArray(),
                             opPass.toCharArray());
        }
        userService.save(u);
        out.println(CLI_OUT_USER_CHG_PASS.getText(u.getName()));
    }

    /**
     * Lists the users in the database.
     *
     * @param nameFilter a filter to filter the list of users. Can be null, in
     * which case all the users are listed.
     * @param active the desired value of the active flag (null means
     * "don't care")
     *
     * @throws PersistenceException if there was an error fetching the users.
     */
    private void listUsers(String nameFilter,
                           Boolean active)
        throws PersistenceException
    {
        List<SimpleUser> l = userService.listUsers(nameFilter,
                                                   active);
        for(SimpleUser u:l) {
            StringBuilder flags=new StringBuilder();
            if (u.isSuperuser()) {
                flags.append(OPT_OPERATED_SUPERUSER); 
            }
            if ((active==null) && u.isActive()) {
                flags.append(OPT_OPERATED_ACTIVE);
            }
            out.print(u.getName());
            if (flags.length()>0) {
                out.print(" ["); //$NON-NLS-1$
                out.print(flags.toString());
                out.print(']');
            }
            out.println();
        }
    }

    /**
     * Deletes the user from the database.
     *
     * @param opUser the name of the user to be deleted.
     *
     * @throws I18NException if there were errors deleting the user, or
     * an attempt was made to delete the admin user.
     */
    private void deleteUser(String opUser) throws I18NException {
        if(ADMIN_USER_NAME.equals(opUser)) {
            throw new I18NException(new I18NBoundMessage1P(
                    CLI_ERR_UNAUTH_DELETE,opUser));
        }
        userService.updateUserActiveStatus(opUser, false);
        out.println(CLI_OUT_USER_DELETED.getText(opUser));
    }

    /**
     * Restores the user in the database.
     *
     * @param opUser the name of the user to be restored.
     *
     * @throws I18NException if there were errors restoring the user.
     */
    private void restoreUser(String opUser)
            throws I18NException
    {
        if(ADMIN_USER_NAME.equals(opUser)) {
            throw new I18NException(new I18NBoundMessage1P(CLI_ERR_UNAUTH_RESTORE,
                                                           opUser));
        }
        userService.updateUserActiveStatus(opUser,
                                           true);
        out.println(CLI_OUT_USER_RESTORED.getText(opUser));
    }

    /**
     * Changes the user's superuser flag.
     *
     * @param opUser the name of the user to be changed.
     * @param superuser the new value of the superuser flag.
     *
     * @throws I18NException if there were errors setting the flag.
     */
    private void changeSuperuser(String opUser,
                                 Boolean superuser)
        throws I18NException
    {
        if(ADMIN_USER_NAME.equals(opUser)) {
            throw new I18NException(new I18NBoundMessage1P(CLI_ERR_UNAUTH_CHANGE_SUPERUSER,
                                                           opUser));
        }
        SimpleUser user = fetchUser(opUser);
        user.setSuperuser(superuser);
        userService.save(user);
        out.println(CLI_OUT_USER_CHG_SUPERUSER.getText(opUser));
    }

    /**
     * Adds the supplied user to the database
     *
     * @param opUser the name of the new user
     * @param opPass the password for the new user
     * @param superuser the new value of the superuser flag.
     *
     * @throws PersistenceException if there was an error adding
     * the new user
     */
    private void addUser
        (String opUser,
         String opPass,
         Boolean superuser)
        throws PersistenceException
    {
        SimpleUser u = new SimpleUser();
        u.setName(opUser);
        u.setPassword(opPass.toCharArray());
        if (superuser!=null) {
            u.setSuperuser(superuser);
        }
        userService.save(u);
        out.println(CLI_OUT_USER_CREATED.getText(u.getName()));
    }

    /**
     * Enum to aid authorization of running various user commands
     */
    private enum Authorization {
        ADD_USER,
        DELETE_USER,
        RESTORE_USER,
        CHANGE_PASSWORD,
        CHANGE_SUPERUSER,
        LIST_USERS {
            @Override
            public void authorize(SimpleUser simpleUser, String password)
                    throws I18NException {
                validateUser(simpleUser, password);
            }}
        ;

        public void authorize(SimpleUser simpleUser, String password) throws I18NException {
            validateUser(simpleUser,password);
            if(!simpleUser.getName().toLowerCase().equals(ADMIN_USER_NAME)) {
                throw new I18NException(CLI_UNAUTHORIZED_ACTION);
            }
        }
        private static void validateUser(SimpleUser simpleUser,
                                         String password)
                throws I18NException
        {
            try {
                simpleUser.validatePassword(password.toCharArray());
            } catch (PersistenceException e) {
                e.printStackTrace();
                throw new I18NException(CLI_ERR_INVALID_LOGIN);
            }
            if (!simpleUser.isActive()) {
                throw new I18NException(CLI_ERR_INVALID_LOGIN);
            }
        }
    }

    /**
     * Returns the options accepted by the CLI.
     *
     * @return the options accepted by the CLI
     */
    @SuppressWarnings("static-access")
    private static Options options() {
        Options opts = new Options();
        opts.addOption(OptionBuilder.hasArg().
                withArgName(CLI_ARG_LOGIN_VALUE.getText()).
                withDescription(CLI_PARM_USER.getText()).
                isRequired(true).create(OPT_CURRENT_USER));
        opts.addOption(OptionBuilder.hasArg().
                withArgName(CLI_ARG_LOGIN_PASSWORD_VALUE.getText()).
                withDescription(CLI_PARM_PASSWORD.getText()).
                isRequired(false).create(OPT_CURRENT_PASSWORD));

        OptionGroup commands = new OptionGroup();
        commands.setRequired(true);
        commands.addOption(OptionBuilder.withLongOpt(CMD_LIST_USERS).
                withDescription(CLI_CMD_LIST_USERS.getText()).
                isRequired().create());
        commands.addOption(OptionBuilder.withLongOpt(CMD_ADD_USER).
                withDescription(CLI_CMD_ADD_USER.getText()).
                isRequired().create());
        commands.addOption(OptionBuilder.withLongOpt(CMD_DELETE_USER).
                withDescription(CLI_CMD_DELETE_USER.getText()).
                isRequired().create());
        commands.addOption(OptionBuilder.withLongOpt(CMD_RESTORE_USER).
                withDescription(CLI_CMD_RESTORE_USER.getText()).
                isRequired().create());
        commands.addOption(OptionBuilder.withLongOpt(CMD_CHANGE_PASS).
                withDescription(CLI_CMD_CHANGE_PASSWORD.getText()).
                isRequired().create());
        commands.addOption(OptionBuilder.withLongOpt(CMD_CHANGE_SUPERUSER).
                withDescription(CLI_CMD_CHANGE_SUPERUSER.getText()).
                isRequired().create());
        opts.addOptionGroup(commands);
        //Add optional arguments
        opts.addOption(OptionBuilder.withLongOpt("username"). //$NON-NLS-1$
                hasArg().withArgName(CLI_ARG_USER_NAME_VALUE.getText()).
                withDescription(CLI_PARM_OP_USER.getText()).
                isRequired(false).create(OPT_OPERATED_USER));
        opts.addOption(OptionBuilder.withLongOpt("password"). //$NON-NLS-1$
                hasArg().withArgName(CLI_ARG_USER_PASSWORD_VALUE.getText()).
                withDescription(CLI_PARM_OP_PASSWORD.getText()).
                isRequired(false).create(OPT_OPERATED_PASSWORD));
        opts.addOption(OptionBuilder.withLongOpt("superuser"). //$NON-NLS-1$
                hasArg().withArgName(CLI_ARG_USER_SUPERUSER_VALUE.getText()).
                withDescription(CLI_PARM_OP_SUPERUSER.getText()).
                isRequired(false).create(OPT_OPERATED_SUPERUSER));
        opts.addOption(OptionBuilder.withLongOpt("active"). //$NON-NLS-1$
                hasArg().withArgName(CLI_ARG_USER_ACTIVE_VALUE.getText()).
                withDescription(CLI_PARM_OP_ACTIVE.getText()).
                isRequired(false).create(OPT_OPERATED_ACTIVE));
        return opts;
    }

    /**
     * Prints the message onto the error output stream
     * @param msg the error message
     */
    private void printError(String msg) {
        err.println(msg);
    }

    /**
     * Prints the usage onto the error output stream
     */
    private void printUsage() {
        HelpFormatter h = new HelpFormatter();
        final int width = 100;
        final int leftPad = 4;
        final int descPad = 4;
        final String LS = SystemUtils.LINE_SEPARATOR;
        final String l = "-" + OPT_CURRENT_USER + " <" + CLI_ARG_LOGIN_VALUE.getText() + "> "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final String p = "-" + OPT_CURRENT_PASSWORD + " <" + CLI_ARG_LOGIN_PASSWORD_VALUE.getText() + "> "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final String u = "-" + OPT_OPERATED_USER + " <" + CLI_ARG_USER_NAME_VALUE.getText() + "> "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final String up = "-" + OPT_OPERATED_PASSWORD + " <" + CLI_ARG_USER_PASSWORD_VALUE.getText() + "> "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final String us = "-" + OPT_OPERATED_SUPERUSER + " " + OPT_YES + '|' +OPT_NO; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final String ua = "-" + OPT_OPERATED_ACTIVE + " " + OPT_YES + '|' +OPT_NO; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final String prefix = CMD_NAME + " " + l + p ; //$NON-NLS-1$
        final String s = prefix + "--" + CMD_LIST_USERS + " [" + ua + "] " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                LS + prefix + "--" + CMD_ADD_USER + " " + u + " " + up + " [" + us + "] " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                LS + prefix + "--" + CMD_DELETE_USER + " " + u + //$NON-NLS-1$ //$NON-NLS-2$
                LS + prefix + "--" + CMD_RESTORE_USER + " " + u + //$NON-NLS-1$ //$NON-NLS-2$
                LS + prefix + "--" + CMD_CHANGE_PASS + " " + up + " [" + u + "]" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                LS + prefix + "--" + CMD_CHANGE_SUPERUSER + " " + u + " "+ us + LS; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final PrintWriter writer = new PrintWriter(err);
        h.printHelp(writer, width, s, CLI_DESC_OPTIONS_HEADER.getText(),
                options(), leftPad, descPad, "", false); //$NON-NLS-1$
        writer.flush();
    }
    private PrintStream out = System.out;
    private PrintStream err = System.err;
    private Console console = System.console();
    private static final String CMD_LIST_USERS = "listUsers"; //$NON-NLS-1$
    private static final String CMD_ADD_USER = "addUser"; //$NON-NLS-1$
    private static final String CMD_DELETE_USER = "deleteUser"; //$NON-NLS-1$
    private static final String CMD_RESTORE_USER = "restoreUser"; //$NON-NLS-1$
    private static final String CMD_CHANGE_PASS = "changePassword"; //$NON-NLS-1$
    private static final String CMD_CHANGE_SUPERUSER = "changeSuperuser"; //$NON-NLS-1$
    private static final String OPT_CURRENT_USER = "u"; //$NON-NLS-1$
    private static final String OPT_CURRENT_PASSWORD = "p"; //$NON-NLS-1$
    private static final String OPT_OPERATED_USER = "n"; //$NON-NLS-1$
    private static final String OPT_OPERATED_PASSWORD = "w"; //$NON-NLS-1$
    private static final String OPT_OPERATED_SUPERUSER = "s"; //$NON-NLS-1$
    private static final String OPT_OPERATED_ACTIVE = "a"; //$NON-NLS-1$
    private static final String ADMIN_USER_NAME = "admin"; //$NON-NLS-1$

    private static final String OPT_YES = "y"; //$NON-NLS-1$
    private static final String OPT_NO = "n"; //$NON-NLS-1$
    static final String CMD_NAME = "orsadmin"; //$NON-NLS-1$
    /**
     * provides access to user objects
     */
    private UserService userService;
    /**
     * indicates if the service is running or not
     */
    private final AtomicBoolean running = new AtomicBoolean(false);
}
