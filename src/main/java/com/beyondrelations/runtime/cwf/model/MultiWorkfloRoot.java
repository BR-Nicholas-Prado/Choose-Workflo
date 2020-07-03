/* see ../../../../../LICENSE for release details */
package com.beyondrelations.runtime.cwf.model;

import java.nio.file.Path;

/**

*/
public class MultiWorkfloRoot
{


	private final Path rootDir;
	private final boolean isMicrotoolsStartup; // fix enum


	/**  */
	public MultiWorkfloRoot(
			Path where,
			boolean whetherOldStyle
	) {
		if ( where == null )
			throw new RuntimeException( "path must not be null" );
		rootDir = where;
		isMicrotoolsStartup = whetherOldStyle;
	}


	public Path getRootDir()
	{
		return rootDir;
	}


	public boolean isMicrotoolsStartup()
	{
		return isMicrotoolsStartup;
	}



}


















