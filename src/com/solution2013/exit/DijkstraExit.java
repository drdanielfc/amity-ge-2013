package com.solution2013.exit;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.csc2013.DungeonMaze.BoxType;
import com.solution2013.Dijkstras;
import com.solution2013.field.FieldMap;
import com.solution2013.field.Space;
import com.solution2013.field.SpaceWrapper;
import com.solution2013.utils.Permutations;

public class DijkstraExit
{

	private int currentKeys;		// How many keys we have right now
	private Point currentLocation;
	private HashMap<Point, Space> currentMap;

	public DijkstraExit(int keys, FieldMap map)
	{
		this.currentKeys = keys;
		this.currentLocation = map.getLocation();
		this.currentMap = map.getMap();
	}

	public Stack<SpaceWrapper> toExit()
	{
		List<Path> solved = new ArrayList<>();		// List of paths that lead to an exit

		List<Path> paths = new ArrayList<>();								// List of paths we are still evaluating
		paths.add(new Path(currentMap, currentLocation, currentKeys));		// Add an initial path which we'll branch off of

		while (paths.size() > 0)
		{
//			System.out.println(paths.get(0));
			
			Iterator<Path> itr = paths.iterator();
			while (itr.hasNext())
			{
				// Find shortest path to exit. Add it to the solved list.

				Path p = itr.next();

				Dijkstras d = new Dijkstras(p.getKeys(), p.getLocation(), p.getMap());

				Stack<SpaceWrapper> toExit = d.shortestToType(p.getLocation(), BoxType.Exit);
				if (toExit != null)
				{
					p.addToPath(toExit);		// Add going to the exit to the path
					solved.add(p);				// Add the path to the solved list
					//					itr.remove();				// Remove the path from the ones we are still considering
				}
			}
			
			List<Path> tempPaths = new ArrayList<>();

			itr = paths.iterator();
			while (itr.hasNext())
			{
				// Get keys
				Path p = itr.next();

				// Get a list of all keys that we can walk to w/o going through doors
				Dijkstras k = new Dijkstras(p.getKeys(), p.getLocation(), p.getMap());

				List<Space> keys = new ArrayList<>();
				for (Space s : p.getMap().values())
				{
					// Do not reuse the shortestToType here b/c it will be inakzhe after we've gone to a key
					if (s.getType() == BoxType.Key && k.shortestToType(p.getLocation(), s) != null)
						keys.add(s);
				}
				
//				System.out.println(keys);

				if (keys.size() == 0 && p.getKeys() == 0)		// There are no more keys to get and we're outta keys. Kill the path.
				{
					itr.remove();
					continue;
				}

				Path next = p.clone();

				// Find paths to go to 0,1,...,n keys. This is basically a greedy salesman algorithm repeated for multiple keys.
				// The 0 keys path is already accounted for - it's the one we're in
				for (int i = 1; i <= keys.size(); i++)
				{
					next.addToPath(k.shortestToType(p.getLocation(), BoxType.Key));
					tempPaths.add(next);

					next = next.clone();
				}
			}
			
			paths.addAll(tempPaths);			// To avoid conmod
			tempPaths = new ArrayList<>();

			itr = paths.iterator();
			while (itr.hasNext())
			{
				// Get doors
				Path p = itr.next();

				itr.remove();		// Don't need the original path anymore. We'll either give up on it or branch from it.
				if (p.getKeys() == 0)
				{
					// We can't go to a door from this path because there's no keys left
					continue;
				}

				// Get a list of all doors that we can walk to w/o going through doors
				Dijkstras k = new Dijkstras(p.getKeys(), p.getLocation(), p.getMap());

				for (Space s : p.getMap().values())
				{
					if (s.getType() == BoxType.Door)		// TODO: Check to make sure the shortestToType part works w/ doors as the goal
					{
						Stack<SpaceWrapper> toDoor = k.shortestToType(p.getLocation(), s);
						if (toDoor == null)		// No possible path to that door
							continue;

						Path next = p.clone();

						next.addToPath(toDoor);
						tempPaths.add(next);
					}
				}
			}
			
			paths.addAll(tempPaths);
			tempPaths = new ArrayList<>();
		}

		Path ideal = null;
		for (Path s : solved)
		{
			if (ideal == null || s.getPath().size() < ideal.getPath().size())
				ideal = s;
		}

		System.out.println(ideal);

		return null;
	}

}