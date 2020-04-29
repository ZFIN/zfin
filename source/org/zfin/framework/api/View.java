package org.zfin.framework.api;

public class View {

        // Generic Views
        public static class Default { }

        public static class API extends Default { }

        public static class Disease extends API { }
        public static class Expression extends API { }
        public static class SequenceAPI extends API { }
        public static class OrthologyAPI extends API { }
        public static class ConstructAPI extends API { }
        public static class MarkerRelationshipAPI extends API { }
        public static class GeneExpressionAPI extends API { }
        public static class CitationsAPI extends API { }

}
