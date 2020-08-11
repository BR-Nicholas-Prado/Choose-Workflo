/* see ../../../../../LICENSE for release details */
package com.beyondrelations.runtime.cwf;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
		Collection<Path> wfJarsOfRoot = ChecksForFiles.workfloFilesOf( wfParentChosen );
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
		System.out.println( "Available jars of "+ wfParentChosen.getRootDir() );
		Map<String, WfPathReply> inputElements = new TreeMap<>();
		Object[] sortedPaths = wfJarsOfRoot.toArray();
		Arrays.sort( sortedPaths );
		final int nonConfigOptionCount = 3, zeroPadThreshold = 9;
		int configOptions = wfJarsOfRoot.size() + nonConfigOptionCount;
		boolean optionsOutnumberPaddingThreshold = configOptions > zeroPadThreshold;
		String zeroPadStyle = optionsOutnumberPaddingThreshold ? "%02d" : "%d";
		int ind = 0;
		for ( Object candidate : sortedPaths )
		{
			inputElements.put(
					String.format( zeroPadStyle, ind ),
					new WfPathReply( context, (Path)candidate ) );
			ind++;
		}
		inputElements.put(
				String.format( zeroPadStyle, ind ),
				new WfPathReply( SessionAspect.JRE_FOLDER, null )  );
		ind++;
		inputElements.put(
				String.format( zeroPadStyle, ind ),
				new WfPathReply( SessionAspect.PROJECTS_FOLDER, null )  );
		ind++;
		inputElements.put(
				String.format( zeroPadStyle, ind ),
				new WfPathReply( SessionAspect.QUIT, null )  );
		ind++;
		showOptions( inputElements, context );
		// N- harvest choice
		int attempts = 10;
		WfPathReply userChoice = null;
		while ( attempts > 0 )
		{
			System.out.print( "-- " );
			String literalInput = input.next();
			if ( literalInput.isEmpty() )
				continue;
			userChoice = inputElements.get( literalInput );
			if ( userChoice == null
					&& optionsOutnumberPaddingThreshold
					&& literalInput.length() == 1 )
			{
				userChoice = inputElements.get( "0"+ literalInput );
			}
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
		else if ( userChoice.intention == SessionAspect.QUIT )
			System.exit( 1 );
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
		SessionAspect context = SessionAspect.PROJECTS_FOLDER;
		System.out.println( "Available project roots" );

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
		Map<String, WfPathReply> inputElements = new TreeMap<>();
		Object[] sortedPaths = roots.getJvmLocations().toArray();
		Arrays.sort( sortedPaths );
		final int nonConfigOptionCount = 2, zeroPadThreshold = 9;
		int configOptions = roots.getJvmLocations().size() + nonConfigOptionCount;
		boolean optionsOutnumberPaddingThreshold = configOptions > zeroPadThreshold;
		String zeroPadStyle = optionsOutnumberPaddingThreshold ? "%02d" : "%d";
		int ind = 0;
		for ( Object candidate : sortedPaths )
		{
			inputElements.put(
					String.format( zeroPadStyle, ind ),
					new WfPathReply( context, (Path)candidate ) );
			ind++;
		}
		inputElements.put(
				String.format( zeroPadStyle, ind ),
				new WfPathReply( SessionAspect.PROJECTS_FOLDER, null )  );
		ind++;
		inputElements.put(
				String.format( zeroPadStyle, ind ),
				new WfPathReply( SessionAspect.QUIT, null )  );
		ind++;
		showOptions( inputElements, context );
		// N- harvest choice
		int attempts = 10;
		WfPathReply userChoice = null;
		while ( attempts > 0 )
		{
			System.out.print( "-- " );
			String literalInput = input.next();
			if ( literalInput.isEmpty() )
				continue;
			userChoice = inputElements.get( literalInput );
			if ( userChoice == null
					&& optionsOutnumberPaddingThreshold
					&& literalInput.length() == 1 )
			{
				userChoice = inputElements.get( "0"+ literalInput );
			}
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
		else if ( userChoice.intention == SessionAspect.QUIT )
			System.exit( 1 );
		else if ( userChoice.intention == context )
			chooseWorkfloInstance();
		else
			throw new RuntimeException( here +"used non config choice, quit" );
	}


	private void chooseConfigFile()
	{
		final String here = cl +"ccf ";
		SessionAspect context = SessionAspect.CONFIG_FILE;
		Collection<Path> configsForJar = ChecksForFiles.configFilesOf(
				workfloChosen, wfParentChosen .isMicrotoolsStartup() );
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
		Map<String, WfPathReply> inputElements = new TreeMap<>();
		Object[] sortedPaths = configsForJar.toArray();
		Arrays.sort( sortedPaths );
		final int nonConfigOptionCount = 4, zeroPadThreshold = 9;
		int configOptions = configsForJar.size() + nonConfigOptionCount;
		boolean optionsOutnumberPaddingThreshold = configOptions > zeroPadThreshold;
		String zeroPadStyle = optionsOutnumberPaddingThreshold ? "%02d" : "%d";
		int ind = 0;
		for ( Object candidate : sortedPaths )
		{
			inputElements.put(
					String.format( zeroPadStyle, ind ),
					new WfPathReply( context, (Path)candidate ) );
			ind++;
		}
		inputElements.put(
				String.format( zeroPadStyle, ind ),
				new WfPathReply( SessionAspect.WF_FILE, null )  );
		ind++;
		inputElements.put(
				String.format( zeroPadStyle, ind ),
				new WfPathReply( SessionAspect.PROJECTS_FOLDER, null )  );
		ind++;
		inputElements.put(
				String.format( zeroPadStyle, ind ),
				new WfPathReply( SessionAspect.JRE_FOLDER, null )  );
		ind++;
		inputElements.put(
				String.format( zeroPadStyle, ind ),
				new WfPathReply( SessionAspect.QUIT, null )  );
		ind++;
		showOptions( inputElements, context );
		// N- harvest choice
		int attempts = 10;
		WfPathReply userChoice = null;
		while ( attempts > 0 )
		{
			System.out.print( "-- " );
			String literalInput = input.next();
			if ( literalInput.isEmpty() )
				continue;
			userChoice = inputElements.get( literalInput );
			if ( userChoice == null
					&& optionsOutnumberPaddingThreshold
					&& literalInput.length() == 1 )
			{
				userChoice = inputElements.get( "0"+ literalInput );
			}
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
		else if ( userChoice.intention == SessionAspect.QUIT )
			System.exit( 1 );
		else
			chooseWorkfloInstance();
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
		// N- prep list of duplicate parents.
		Set<Path> uniqueParents = new HashSet<>();
		Set<Path> nonUniqueParents = new TreeSet<>();
		for ( String key : inputElements.keySet() )
		{
			WfPathReply current = inputElements.get( key );
			if ( current.location == null )
				continue;
			else if ( uniqueParents.contains( current.location.getParent() ) )
				nonUniqueParents.add( current.location.getParent() );
			else
				uniqueParents.add( current.location.getParent() );
		}
		// N- actually show
		for ( String key : inputElements.keySet() )
		{
			WfPathReply current = inputElements.get( key );
			if ( current == null )
				continue;
			if ( current.location == null )
				System.out.println( key +" - "+ current.intention.name() );
			else if ( context != SessionAspect.WF_FILE && context != SessionAspect.JRE_FOLDER )
				System.out.println( key +" - "+ current.location.getFileName() );
			else if ( context == SessionAspect.JRE_FOLDER )
				System.out.println( key +" - "+ current.location
						.getParent().getParent().getFileName() ); // N- grandparent folder bla/jre/bin/java.exe
			else if ( nonUniqueParents.contains( current.location.getParent() ) )
				System.out.println( key +" - "+ current.location
						.getParent().getFileName() +" : "+ current.location
						.getFileName() ); // N- the folder name
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
		QUIT,
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









































































