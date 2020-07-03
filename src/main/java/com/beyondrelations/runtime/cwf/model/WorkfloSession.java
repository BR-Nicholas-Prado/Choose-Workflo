/* see ../../../../../LICENSE for release details */
package com.beyondrelations.runtime.cwf.model;

import java.nio.file.Path;

/**

*/
public class WorkfloSession
{

	private final Path jreLocation;
	private final WorkfloRoot binary;
	private final Path workfloDbInformation;


	/**  */
	public WorkfloSession(
			Path jvm,
			WorkfloRoot jar,
			Path properties
	) {
		if ( jvm == null )
			throw new RuntimeException( "jre must not be null" );
		jreLocation = jvm;
		if ( jar == null )
			throw new RuntimeException( "workflo must not be null" );
		binary = jar;
		if ( properties == null )
			throw new RuntimeException( "properties must not be null" );
		workfloDbInformation = properties;
	}


	public Path getJreLocation()
	{
		return jreLocation;
	}


	public WorkfloRoot getBinary()
	{
		return binary;
	}


	public Path getWorkfloDbInformation()
	{
		return workfloDbInformation;
	}

}


















