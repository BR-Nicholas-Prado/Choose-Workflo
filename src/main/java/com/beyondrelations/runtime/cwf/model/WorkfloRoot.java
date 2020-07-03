/* see ../../../../../LICENSE for release details */
package com.beyondrelations.runtime.cwf.model;

import java.nio.file.Path;

/**

*/
public class WorkfloRoot
{
	private final Path jarFileName;
	private final boolean isMicrotoolsStyle;

	/**  */
	public WorkfloRoot(
			Path where,
			boolean whetherOldStyle
	) {
		if ( where == null )
			throw new RuntimeException( "path must not be null" );
		jarFileName = where;
		isMicrotoolsStyle = whetherOldStyle;
	}


	public Path getJarFileName()
	{
		return jarFileName;
	}


	public boolean isMicrotoolsStyle()
	{
		return isMicrotoolsStyle;
	}




}


















