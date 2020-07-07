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

import ws.nzen.format.eno.Eno;
import ws.nzen.format.eno.FieldList;
import ws.nzen.format.eno.Section;
import ws.nzen.format.eno.Value;

/**

*/
public class Initialization
{

	private static final String defaultConfigFilename = "config.eno";
	private static final String enoSectionMain = "choose workflo";
	private static final String enoListJre = "jre";
	private static final String enoSectionMultiWf = "multiworkflo";
	private static final String enoMwfFieldDir = "rootDir";
	private static final String enoMwfFieldIsMts = "isMicrotoolsStartup";


	public static void main( String[] args )
	{
		final String here = "<>i.m ";
		File defaultConfig = new File( defaultConfigFilename );
		Initialization mainThread;
		try
		{
			mainThread = ! defaultConfig.exists()
					? new Initialization( hardCodedConfig() )
					: new Initialization( configFrom( defaultConfig ) );
		}
		catch ( SecurityException se )
		{
			System.err.println( here +"could not check if "+ defaultConfigFilename
					+" exists because "+ se );
			mainThread =  new Initialization( hardCodedConfig() );
		}
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


	private static Configuration configFrom( File hasValues )
	{
		final String here = "<>i.c ";
		Configuration fallback = hardCodedConfig();
		Section enoDoc = null;
		try
		{
			enoDoc = new Eno().deserialize( hasValues.toPath() );
		}
		catch ( IOException ie )
		{
			System.err.println( here +"deserialize "+ hasValues
					+" because "+ ie );
			return fallback;
		}
		Section chwfInfo = enoDoc.optionalSection( enoSectionMain );
		if ( chwfInfo == null )
		{
			System.err.println( here + hasValues +" has no  "
						+ enoSectionMain +" section, using hardcode" );
			return fallback;
		}
		Configuration suppliedInfo = new Configuration();
		FieldList jreAvailable = chwfInfo.optionalList( enoListJre );
		for ( String path : jreAvailable.optionalStringValues() )
		{
			suppliedInfo.addJvmLocation( Paths.get( path ) );
		}
		if ( suppliedInfo.getJvmLocations().isEmpty() )
		{
			System.err.println( here + hasValues +" has no  "
					+ enoListJre +" list, using hardcode" );
			for ( Path whatever : fallback.getJvmLocations() )
				suppliedInfo.addJvmLocation( whatever );
		}

		Section projectRoots = chwfInfo.optionalSection( enoSectionMultiWf );
		if ( projectRoots == null )
		{
			System.err.println( here + hasValues +" has no "
					+ enoSectionMultiWf +" section, using hardcode" );
			for ( MultiWorkfloRoot whatever : fallback.getGroupFolder() )
				suppliedInfo.addWorkfloRoot( whatever );
		}
		List<Section> actualRoots = projectRoots.sections();
		if ( actualRoots.isEmpty() )
		{
			System.err.println( here + hasValues +" has "
					+ enoSectionMultiWf +" with no concrete sections, using hardcode" );
			for ( MultiWorkfloRoot whatever : fallback.getGroupFolder() )
				suppliedInfo.addWorkfloRoot( whatever );
		}
		for ( Section rootDesc : actualRoots )
		{
			Value wrappedPath = (Value)rootDesc.optionalField( enoMwfFieldDir );
			Value strBool = (Value)rootDesc.optionalField( enoMwfFieldIsMts );
			if ( wrappedPath == null || strBool == null )
			{
				System.out.println( here + hasValues +" has "
						+ enoSectionMultiWf +" with "+ rootDesc.getName() +" with missing "
						+ ( wrappedPath == null ? enoMwfFieldDir : enoMwfFieldIsMts ) );
				continue;
			}
			suppliedInfo.addWorkfloRoot( new MultiWorkfloRoot(
					Paths.get( wrappedPath.optionalStringValue() ),
					strBool.optionalStringValue().equals( "true" ) ) );
		}
		if ( suppliedInfo.getGroupFolder().isEmpty() )
		{
			System.err.println( here + hasValues +" has "
					+ enoSectionMultiWf +" with no valid sections, using hardcode" );
			for ( MultiWorkfloRoot whatever : fallback.getGroupFolder() )
				suppliedInfo.addWorkfloRoot( whatever );
		}

		return suppliedInfo;
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






















































