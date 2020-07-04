/* see ../../../../../LICENSE for release details */
package com.beyondrelations.runtime.cwf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import com.beyondrelations.runtime.cwf.model.Configuration;
import com.beyondrelations.runtime.cwf.model.MultiWorkfloRoot;
import com.beyondrelations.runtime.cwf.model.WorkfloSession;

/**

*/
public class Initialization
{


	public static void main( String[] args )
	{
		/*
		cli finds config source
		build configuration with parser
		offer choices
			check folders
		start workflo
		*/
		Initialization mainThread = new Initialization( hardCodedConfig() );
		while ( true )
		{
			WorkfloSession finalDecisions = mainThread.offerChoices();
			if ( finalDecisions == null )
				break;
			mainThread.runWorkflo( finalDecisions );
		}
	}


	public static Configuration hardCodedConfig()
	{
		Configuration info = new Configuration();
		Path osSubstitutionAsRelative = Paths.get( "java" );
		Path validOption = Paths.get( "D:\\Programs64b\\base\\java\\jdk_v14.0_openjdk\\bin\\java.exe" );
		info.addJvmLocation( validOption );
		info.addJvmLocation( osSubstitutionAsRelative );
		MultiWorkfloRoot mwDeliveries = new MultiWorkfloRoot(
				// Paths.get( "U:\\\\microworxDeliveries" ), false );
				Paths.get( "src\\test\\resources\\imitation" ), false );
		// improve add abandoned gitdeliveries
		info.addWorkfloRoot( mwDeliveries );

		return info;
	}


	private final Configuration paths;


	/**  */
	public Initialization(
			Configuration info
	) {
		if ( info == null )
			throw new RuntimeException( "config must not be null" );
		paths = info;
	}


	public WorkfloSession offerChoices()
	{
		SessionViaStdin hearsChoices = new SessionViaStdin( paths );
		return hearsChoices.requestDecisions();
	}


	public void runWorkflo( WorkfloSession runInfo )
	{
		ProcessBuilder shellFactory = new ProcessBuilder(
				asCommandSentence( runInfo ) );
		shellFactory.directory( runInfo.getBinary()
				.getJarFolder().toFile() );
		shellFactory.inheritIO();
		try
		{
			shellFactory.start().waitFor();
		}
		catch ( IOException ie )
		{
			System.err.println( "Couldn't launch jar because "+ ie );
		}
		catch ( InterruptedException ie )
		{
			System.err.println( "Programmatically told to quit via "+ ie );
			return;
		}
	}


	private List<String> asCommandSentence( WorkfloSession runInfo )
	{
		List<String> commandComponents = new LinkedList<String>();
		commandComponents.add( runInfo.getJreLocation().toString() );
		// Improve any jre tuning flags here
		commandComponents.add( "-jar" );
		commandComponents.add( runInfo.getBinary()
				.getJarFileName().toString() );
		commandComponents.add( runInfo.getBinary()
				.isMicrotoolsStyle() ? "-c" : "-p" );
		commandComponents.add( "config"+ File.separator
				+ runInfo.getWorkfloDbInformation().getFileName().toString() );
		if ( runInfo.getBinary().isMicrotoolsStyle() )
		{
			commandComponents.add( "-erp" );
			commandComponents.add( "-am" );
			commandComponents.add( "-debug" );
			// N- ignoring login flags
		}
		return commandComponents;
	}

}






















































