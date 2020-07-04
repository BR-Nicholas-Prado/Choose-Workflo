/* see ../../../../../LICENSE for release details */
package com.beyondrelations.runtime.cwf;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import com.beyondrelations.runtime.cwf.model.Configuration;
import com.beyondrelations.runtime.cwf.model.MultiWorkfloRoot;
import com.beyondrelations.runtime.cwf.model.WorkfloRoot;
import com.beyondrelations.runtime.cwf.model.WorkfloSession;

/**

*/
public class SessionViaStdin
{

	private static final String cl = "svs.";
	private final Configuration roots;
	private final Scanner input = new Scanner( System.in );
	private Path jreChosen = null;
	private Path workfloChosen = null;
	private Path configChosen = null;
	private MultiWorkfloRoot wfParentChosen = null;


	/**  */
	public SessionViaStdin(
			Configuration info
	) {
		if ( info == null )
			throw new RuntimeException( "config must not be null" );
		else if ( info.getJvmLocations().isEmpty() )
			throw new RuntimeException( "config must include jre" );
		else if ( info.getGroupFolder().isEmpty() )
			throw new RuntimeException( "config must include workflo folder" );
		roots = info;
	}


	public WorkfloSession requestDecisions()
	{
		// N- assume the first, allow changing later, if more exist
		jreChosen = roots.getJvmLocations().get( 0 );
		wfParentChosen = roots.getGroupFolder().get( 0 );
		chooseWorkfloInstance();
		return new WorkfloSession(
				jreChosen,
				new WorkfloRoot( workfloChosen, wfParentChosen.isMicrotoolsStartup() ),
				configChosen );
	}


	private void chooseWorkfloInstance()
	{
		final String here = cl +"cwi ";
		SessionAspect context = SessionAspect.WF_FILE;
		Collection<Path> wfJarsOfRoot = workfloFilesOf( wfParentChosen );
		if ( wfJarsOfRoot.isEmpty() )
		{
			System.out.println( "No jars in "+ wfParentChosen );
			chooseMultiWorkfloRoot();
			return; // N- assuming it resolved the entire selection already
		}
		else if ( wfJarsOfRoot.size() == 1 )
		{
			noFallbackOffer(
					"jar",
					wfJarsOfRoot.iterator().next(),
					context );
			return;
		}
		System.out.println( "Available jars of "+ wfParentChosen );
		Map<String, WfPathReply> inputElements = new HashMap<>();
		int ind = 0;
		for ( Path candidate : wfJarsOfRoot )
		{
			inputElements.put( Integer.toString( ind ),
					new WfPathReply( context, candidate ) );
			ind++;
		}
		inputElements.put( Integer.toString( ind ),
				new WfPathReply( SessionAspect.JRE_FOLDER, null )  );
		ind++;
		inputElements.put( Integer.toString( ind ),
				new WfPathReply( SessionAspect.PROJECTS_FOLDER, null )  );
		ind++;
		inputElements.put( Integer.toString( ind ),
				new WfPathReply( SessionAspect.UNKNOWN, null )  );
		ind++;
		showOptions( inputElements, context );
		// N- harvest choice
		int attempts = 10;
		WfPathReply userChoice = null;
		while ( attempts > 0 )
		{
			String literalInput = input.next();
			if ( literalInput.isEmpty() )
				continue;
			userChoice = inputElements.get( literalInput );
			if ( userChoice != null )
			{
				if ( userChoice.intention == context )
					workfloChosen = userChoice.location;
				break;
			}
			else
				System.out.println( "That's not a valid choice, try another" );
			attempts--;
		}
		if ( attempts < 1 )
			throw new RuntimeException( here +"ten abortive tries is enough to, quit" );
		// N- transition to next state
		if ( userChoice.intention == SessionAspect.PROJECTS_FOLDER )
			chooseMultiWorkfloRoot();
		else if ( userChoice.intention == SessionAspect.JRE_FOLDER )
			chooseJavaRuntime();
		else if ( userChoice.intention != context )
			throw new RuntimeException( here +"used non config choice, quit" );
		else
			chooseConfigFile();
	}


	private void chooseMultiWorkfloRoot()
	{
		final String here = cl +"cmwr ";
		// N- assuming we fell back here such that initial default isn't appropriate
		if ( roots.getGroupFolder().size() == 1 )
			throw new RuntimeException( here +"no other multiworkflo root exists, quit" );

		throw new RuntimeException( "cmwr not done" );
	}


	private void chooseJavaRuntime()
	{
		final String here = cl +"cjr ";
		// N- assuming we fell back here such that initial default isn't appropriate
		if ( roots.getJvmLocations().size() == 1 )
			throw new RuntimeException( here +"no other jre exists, quit" );
		SessionAspect context = SessionAspect.JRE_FOLDER;
		System.out.println( "Available java binaries" );
		Map<String, WfPathReply> inputElements = new HashMap<>();
		int ind = 0;
		for ( Path candidate : roots.getJvmLocations() )
		{
			inputElements.put( Integer.toString( ind ),
					new WfPathReply( context, candidate ) );
			ind++;
		}
		inputElements.put( Integer.toString( ind ),
				new WfPathReply( SessionAspect.PROJECTS_FOLDER, null )  );
		ind++;
		inputElements.put( Integer.toString( ind ),
				new WfPathReply( SessionAspect.UNKNOWN, null )  );
		ind++;
		showOptions( inputElements, context );
		// N- harvest choice
		int attempts = 10;
		WfPathReply userChoice = null;
		while ( attempts > 0 )
		{
			String literalInput = input.next();
			if ( literalInput.isEmpty() )
				continue;
			userChoice = inputElements.get( literalInput );
			if ( userChoice != null )
			{
				if ( userChoice.intention == context )
					jreChosen = userChoice.location;
				break;
			}
			else
				System.out.println( "That's not a valid choice, try another" );
			attempts--;
		}
		if ( attempts < 1 )
			throw new RuntimeException( here +"ten abortive tries is enough to, quit" );
		// N- transition to next state
		if ( userChoice.intention == SessionAspect.PROJECTS_FOLDER )
			chooseMultiWorkfloRoot();
		else if ( userChoice.intention == context )
			chooseWorkfloInstance();
		else
			throw new RuntimeException( here +"used non config choice, quit" );
	}


	private void chooseConfigFile()
	{
		final String here = cl +"ccf ";
		SessionAspect context = SessionAspect.CONFIG_FILE;
		Collection<Path> configsForJar = configFilesOf( workfloChosen );
		if ( configsForJar.isEmpty() )
		{
			System.out.println( "No configs for "+ workfloChosen.getParent() );
			chooseWorkfloInstance();
			return; // N- assuming it resolved the entire selection already
		}
		else if ( configsForJar.size() == 1 )
		{
			noFallbackOffer(
					"config",
					configsForJar.iterator().next(),
					context );
			return;
		}
		System.out.println( "Available config of "+ workfloChosen.getParent() );
		Map<String, WfPathReply> inputElements = new HashMap<>();
		int ind = 0;
		for ( Path candidate : configsForJar )
		{
			inputElements.put( Integer.toString( ind ),
					new WfPathReply( context, candidate ) );
			ind++;
		}
		inputElements.put( Integer.toString( ind ),
				new WfPathReply( SessionAspect.WF_FILE, null )  );
		ind++;
		inputElements.put( Integer.toString( ind ),
				new WfPathReply( SessionAspect.PROJECTS_FOLDER, null )  );
		ind++;
		inputElements.put( Integer.toString( ind ),
				new WfPathReply( SessionAspect.JRE_FOLDER, null )  );
		ind++;
		inputElements.put( Integer.toString( ind ),
				new WfPathReply( SessionAspect.UNKNOWN, null )  );
		ind++;
		showOptions( inputElements, context );
		// N- harvest choice
		int attempts = 10;
		WfPathReply userChoice = null;
		while ( attempts > 0 )
		{
			String literalInput = input.next();
			if ( literalInput.isEmpty() )
				continue;
			userChoice = inputElements.get( literalInput );
			if ( userChoice != null )
			{
				if ( userChoice.intention != context )
					break;
				else
				{
					configChosen = userChoice.location;
					return; // N- unroll the stack, selection complete
				}
			}
			else
				System.out.println( "That's not a valid choice, try another" );
			attempts--;
		}
		if ( attempts < 1 )
			throw new RuntimeException( here +"ten abortive tries is enough to, quit" );
		// N- transition to previous state
		if ( userChoice.intention == SessionAspect.PROJECTS_FOLDER )
			chooseMultiWorkfloRoot();
		else if ( userChoice.intention == SessionAspect.JRE_FOLDER )
			chooseJavaRuntime();
		else if ( userChoice.intention != SessionAspect.WF_FILE )
			throw new RuntimeException( here +"used non config choice, quit" );
		else
			chooseWorkfloInstance();
	}


	/** Returns paths to jars themselves */
	private Collection<Path> workfloFilesOf( MultiWorkfloRoot base )
	{
		final String here = cl +"wfo ";
		Collection<Path> relevantFolders = new LinkedList<>();
		try
		{
			File[] allChildrenOfRoot = base.getRootDir().toFile().listFiles(
					new FileFilter()
					{
						@Override
						public boolean accept( File candidate )
						{
							return candidate.isDirectory();
						}
					}
				);
			if ( allChildrenOfRoot == null )
				throw new RuntimeException( here +"invalid multiworkflo root, quit" );
			else if ( allChildrenOfRoot.length == 0 )
				throw new RuntimeException( here +"multi wf folder is empty "
						+ base.getRootDir() +", quit" );
			for ( File someChild : allChildrenOfRoot )
			{
				File[] jarsWithin = someChild.listFiles(
						new FilenameFilter()
						{
							@Override
							public boolean accept( File parentFolder, String name )
							{
								return name.endsWith( "jar" );
							}
						}
					);
				for ( File jar : jarsWithin )
				{
					relevantFolders.add( jar.toPath() );
				}
			}
		}
		catch ( SecurityException se )
		{
			System.err.println( here +" unable to list wf folders of "
					+ base.getRootDir() +" because "+ se );
		}
		return relevantFolders;
	}


	private Collection<Path> configFilesOf( Path workfloJar )
	{
		final String here = cl +"cfo ";
		Path base = Paths.get( workfloJar.getParent().toString(), "config" );
		Collection<Path> relevantFiles = new LinkedList<>();
		try
		{
			final String extension = wfParentChosen
					.isMicrotoolsStartup() ? "properties" : "json";
			File[] dbInfoWithin = base.toFile().listFiles(
					new FilenameFilter()
					{
						@Override
						public boolean accept( File parentFolder, String name )
						{
							return name.endsWith( extension );
						}
					}
				);
			if ( dbInfoWithin == null )
				throw new RuntimeException( here +"config folder next to jar "
						+ workfloJar +", quit" );
			else if ( dbInfoWithin.length == 0 )
				throw new RuntimeException( here +"config folder empty for jar "
						+ workfloJar +", quit" );
			for ( File curr : dbInfoWithin )
			{
				relevantFiles.add( curr.toPath() );
			}
		}
		catch ( SecurityException se )
		{
			System.err.println( here +" unable to list wf folders of "
					+ base +" because "+ se );
		}
		return relevantFiles;
	}


	private void noFallbackOffer(
			String desire,
			Path onlyOption,
			SessionAspect context
	) {
		final String here = cl +"nfo ";
		String relevantPath = context == SessionAspect.WF_FILE
				? onlyOption.getParent().toString() : onlyOption.toString();
		System.out.println( "Only one "+ desire +", "
				+ onlyOption +"; use ? y/n" );
		String reply = input.next();
		if ( reply.toLowerCase().equals( "y" ) )
		{
			switch ( context )
			{
				case WF_FILE :
				{
					workfloChosen = onlyOption;
					chooseConfigFile();
					return;
				}
				case CONFIG_FILE :
				{
					configChosen = onlyOption;
					return;
				}
				case JRE_FOLDER :
				{
					jreChosen = onlyOption;
					chooseWorkfloInstance();
					return;
				}
				default :
				{
					throw new RuntimeException( here +"fallback unhandled for "
							+ context +", quit" );
				}
			}
		}
		else
		{
			switch ( context )
			{
				case WF_FILE :
				{
					chooseMultiWorkfloRoot();
					return;
				}
				case CONFIG_FILE :
				{
					chooseWorkfloInstance();
					return;
				}
				case JRE_FOLDER :
				{
					throw new RuntimeException( here +"no fallback for jre, quit" );
				}
			}
		}
	}


	private void showOptions(
			Map<String, WfPathReply> inputElements, SessionAspect context )
	{
		for ( String key : inputElements.keySet() )
		{
			WfPathReply current = inputElements.get( key );
			if ( current == null )
				continue;
			if ( current.location == null )
				System.out.println( key +" - "+ current.intention.name() );
			else if ( context != SessionAspect.WF_FILE )
				System.out.println( key +" - "+ current.location.getFileName() );
			else
				System.out.println( key +" - "+ current.location
						.getParent().getFileName() ); // N- the folder name
		}
	}


	private class Location
	{
		String description;
		Path inode;
	}


	private enum SessionAspect
	{
		JRE_FOLDER,
		PROJECTS_FOLDER,
		WF_FILE,
		CONFIG_FILE,
		BACK,
		UNKNOWN
		;
	}


	private class WfPathReply
	{
	
		public WfPathReply(
				SessionAspect why,
				Path probablyWhere
		) {
			intention = why;
			location = probablyWhere;
		}

		SessionAspect intention;
		Path location;
	}


}









































































