/* see ../../../../../LICENSE for release details */
package com.beyondrelations.runtime.cwf;

import java.util.Scanner;

import com.beyondrelations.runtime.cwf.model.WorkfloSession;

/**

*/
public class SessionViaStdin
{

	private final Configuration roots;
	private final Scanner input = new Scanner( System.in );


	/**  */
	public SessionViaStdin(
			Configuration info
	) {
		if ( info == null )
			throw new RuntimeException( "config must not be null" );
		roots = info;
	}


	public WorkfloSession requestDecisions()
	{
		/*
		get folder list
		offer workflo folder or back to jre
			if folder has multiple, ask
		get config list
		offer configs
		*/
		

		throw new RuntimeException( "co not done l" );
	}
	/*
	cli finds config source
	build configuration with parser
	offer choices
		check folders
	start workflo
	
	if ( knowsJars.numberOfLocations() < 1 )
	{
		System.err.print( "Config doesn't have any jar locations. Quitting" );
		return "";
	}
	else if ( knowsJars.numberOfLocations() == 1 )
	{
		// just use the first one
		return knowsJars.getLocations().next().getKey();
	}
	else
	{
		System.out.println( "Available jars:" );
		Map<Integer, String> viewToLocation = showOptions( knowsJars.getLocations(),
				knowsJars.numberOfLocations(), new TreeMap<>() );
		System.out.print( "-- ? " );
		Integer userChose = null;
		String literalInput = input.next();
		try
		{
			userChose = Integer.parseInt( literalInput );
		}
		catch ( NumberFormatException nfe )
		{
			System.err.println( "sorry, you get to restart: "
					+ literalInput +" is not a number" );
			System.exit( 0 );
		}
		String jarIdChosen = viewToLocation.get( userChose );
		if ( ! knowsJars.locationsHas( jarIdChosen ) )
		{
			System.err.print( jarIdChosen +" isn't an offered selection. ignored" );
			// IMPROVE actually handle this
			return "";
		}
		else
		{
			return jarIdChosen;
		}
	}


	private Map<Integer, String> showOptions( Iterator<Map.Entry<String, JarLocation>> toShow,
			int howMany, Map<Integer, String> viewToModel )
	{
		if ( howMany <= 10 )
		{
			// single column
			for ( Integer ind = 0; toShow.hasNext(); ind++ )
			{
				Map.Entry<String, JarLocation> keyAndLoc = toShow.next();
				viewToModel.put( ind, keyAndLoc.getKey() );
				System.out.println( formattedIndAndDesc( ind, keyAndLoc
						.getValue().getDesc() ) );
			}
		}
		else
		{
			// two columns
			int halfway = howMany / 2 +1;
			Queue<String> firstColumn = new ArrayDeque<>( halfway );
			String fullDesc;
			int longest = 0;
			for ( Integer ind = 0; ind < halfway; ind++ )
			{
				Map.Entry<String, JarLocation> keyAndLoc = toShow.next();
				viewToModel.put( ind, keyAndLoc.getKey() );
				fullDesc = formattedIndAndDesc( ind, keyAndLoc
						.getValue().getDesc() );
				if ( fullDesc.length() > longest )
				{
					longest = fullDesc.length();
				}
				firstColumn.add( fullDesc );
			}
			longest++;
			String maxWidthFlag = "%-"+ longest +"s";
			for ( Integer ind = halfway; toShow.hasNext(); ind++ )
			{
				Map.Entry<String, JarLocation> keyAndLoc = toShow.next();
				viewToModel.put( ind, keyAndLoc.getKey() );
				String left = ( ! firstColumn.isEmpty() ) ? firstColumn.poll() : "";
				System.out.println( String.format( maxWidthFlag,
						left ) + formattedIndAndDesc(
								ind, keyAndLoc.getValue().getDesc() ) );
			}
			// show stragglers
			while ( ! firstColumn.isEmpty() )
			{
				System.out.println( firstColumn.poll() );
			}
		}
		return viewToModel;
	}
	*/


}









































































