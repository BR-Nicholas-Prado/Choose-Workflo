/* see ../../../../../LICENSE for release details */
package com.beyondrelations.runtime.cwf;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import com.beyondrelations.runtime.cwf.model.MultiWorkfloRoot;

/**

*/
public class Configuration
{

	private List<Path> jvmLocations = new LinkedList<>();
	private List<MultiWorkfloRoot> groupFolder = new LinkedList<>();


	/**  */
	public Configuration()
	{
		// ASK or caller builds the lists outside ?
	}


	public void addJvmLocation( Path where )
	{
		if ( where == null )
			throw new RuntimeException( "path must not be null" );
		jvmLocations.add( where );
	}


	public void addWorkfloRoot( MultiWorkfloRoot another )
	{
		if ( another == null )
			throw new RuntimeException( "workflo group must not be null" );
		groupFolder.add( another );
	}


	public List<Path> getJvmLocations()
	{
		return jvmLocations;
	}


	public List<MultiWorkfloRoot> getGroupFolder()
	{
		return groupFolder;
	}


}


















