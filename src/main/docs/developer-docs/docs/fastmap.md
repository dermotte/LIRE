====== FastMap in Lire ======

Using the FastMap class the feature space can be reduced to an arbitrary number of dimensions. The implementation supports one time fast mapping (e.g. for visualization) and iterative fast mapping. Note that for the iterative way the layout will get worse with the number of inserted points after the first run as the algorithm is not meant to be used like that. So in your implementation do a re-map from time to time.

For iterative FastMap use the SavedPivots class and store it along with the information which descriptor class was used.

    public void testIterativeFastMap() throws InstantiationException, IllegalAccessException {
        // creating the list of user objects ...
        LinkedList<VisualDescriptor> objs = new LinkedList<VisualDescriptor>();
        for (Iterator<Document> documentIterator = docs.iterator(); documentIterator.hasNext();) {
            Document document = documentIterator.next();
            String[] cls = document.getValues(DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM);
            if (cls.length > 0) {
                objs.add(new EdgeHistogramImplementation(cls[0]));
            }
        }
        // create set of non mapped objects in the first place:
        LinkedList<VisualDescriptor> remainingObj = new LinkedList<VisualDescriptor>();
        remainingObj.add(objs.removeLast());

        System.out.println("--------------- < 1st run of iterative fastmap > ---------------");
        long nano = System.nanoTime();
        // create map and get pivots:
        int[][] p = createFastMapForObjects(objs, null);
        nano = System.nanoTime() - nano;
        System.out.println("---< Time taken: ~ " + (nano / (1000 * 1000 * 1000)) + " s");
        // save pivots:
        SavedPivots sp = new SavedPivots(p, objs);

        System.out.println("--------------- < 2nd run of iterative fastmap > ---------------");
        // first create a set of objects for mapping by adding the pivots:
        int[][] pivots = sp.getPivots(remainingObj, EdgeHistogramImplementation.class); // note that the class has to be known.
        p = createFastMapForObjects(remainingObj, pivots);

    }

With the little helper method:

    private int[][] createFastMapForObjects(LinkedList<VisualDescriptor> objs, int[][] savedPivots) {
        // creating the distance matrix for the FastMap process:
        ArrayFastmapDistanceMatrix fdm = new ArrayFastmapDistanceMatrix(objs, new VisualDescriptorDistanceCalculator());
        // note that fastmap needs at least dimensions*2 objects as it needs enough pivots :)
        FastMap fm;
        // use previously defined pivots if available and set to 3 dimensions:
        if (savedPivots == null) fm = new FastMap(fdm, 3);
        else fm = new FastMap(fdm, 3, savedPivots);
        fm.run();
        for (int i = 0; i < fm.getPoints().length; i++) {
            double[] pts = fm.getPoints()[i];
            System.out.print("Obj " + i + ": ( ");
            for (int j = 0; j < pts.length; j++) {
                System.out.print(pts[j] + " ");
            }
            System.out.println(")");
        }
        // return the used pivots for later re-use:
        return fm.getPivots();
    }