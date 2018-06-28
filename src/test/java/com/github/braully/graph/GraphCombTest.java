package com.github.braully.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author strike
 */
public class GraphCombTest extends TestCase {

    private static boolean verbose = true;

    public Map<Integer, List<Integer>> mapInvalidPositions(int k) {
//        int k = 57;
        int ko = k - 2;
        int len = ((ko + 1) * ko) / 2;
        Map<Integer, List<Integer>> excludeMapList = new HashMap<>();
        boolean verbose = false;

        int[] arrup = new int[len];
        int[] arrdown = new int[len];
        int offsetup = ko - 1;
        int up = 0;
        int down = 1;

        for (int i = 0; i < len; i++) {
            arrup[i] = up;
            arrdown[i] = down++;
            if (i == offsetup) {
                up++;
                offsetup += (ko - up);
            }
            if (down == ko + 1) {
                down = up + 1;
            }
            excludeMapList.put(i, new ArrayList<>());
        }
        if (verbose) {
            System.out.println("Seq: ");
        }
        for (int i = 0; i < len; i++) {
            up = arrup[i];
            down = arrdown[i];
            int count = 0;
            int countko = 0;
            StringBuilder sb = new StringBuilder();
            if (verbose) {
                sb.append(String.format("%4d ", i));
                sb.append("|%4d|:");
            }
            List<Integer> listExclude = excludeMapList.get(i);
            for (int j = 0; j < len; j++) {
                if (i != j && (arrdown[j] == up || arrdown[j] == down || arrup[j] == up)) {
                    if (verbose) {
                        sb.append(String.format("%4d ", j));
                    }
                    listExclude.add(j);
                    List<Integer> list2 = excludeMapList.get(j);
                    if (!list2.contains(i)) {
                        list2.add(i);
                    }
                    count++;
                    if (j < ko) {
                        countko++;
                    }
                }
            }
            if (countko >= ko) {
                throw new IllegalStateException("Impossible graph");
            }
            if (verbose) {
                System.out.printf(sb.toString(), count);
                System.out.println();
            }
        }

        for (int i = 0; i < len; i++) {
            Collections.sort(excludeMapList.get(i));
        }
        return excludeMapList;
    }

    private void printArray(int[] arr) {
        int len = arr.length;
        System.out.print("[");
        for (int i = 0; i < len; i++) {
            System.out.print(arr[i]);
            if (i < len - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

    public void testEstrategiaCombinacao() {
//        int k = 57;
        int k = 7;
        int ko = k - 2;
        boolean roolback = false;
        int len = ((ko + 1) * ko) / 2;
        LinkedList<Integer> arr = new LinkedList<>();
        Map<Integer, Integer> countval = new HashMap<>();
        Map<Integer, List<Integer>> mapExcludePosition = mapInvalidPositions(k);

        int maxValCount = 0;
        if (ko != 0) {
            maxValCount = len / ko;
        }

        Map<Integer, List<Integer>> possibilidades = new HashMap<>(len);
        Integer[] targetv = new Integer[ko];

        for (int j = 0; j < ko; j++) {
            targetv[j] = j;
            countval.put(j, 0);
        }
        List<Integer> targetvList = Arrays.asList(targetv);

        for (int i = 0; i < len; i++) {
            possibilidades.put(i, new LinkedList<>(targetvList));
        }

        for (int i = 0; i < ko; i++) {
            arr.add(i);
            List<Integer> posicoesExcluidas = mapExcludePosition.get(i);
            clearEmptyCombination(i, i, countval, maxValCount, possibilidades, posicoesExcluidas);
        }

        int longest = 0;

        while (arr.size() < len && arr.size() >= ko) {
            int pos = arr.size();
            LinkedList<Integer> list = (LinkedList<Integer>) possibilidades.get(pos);
            List<Integer> posicoesExcluidas = mapExcludePosition.get(pos);
            if (roolback || list.isEmpty()) {
                Integer valRollback = arr.pollLast();
                /*  rollback */
//                if (verbose) {
//                    System.out.print("deadlock: ");
//                    try {
//                        System.out.print(arr.get(ko));
//                    } catch (Exception e) {
//                    }
//                    System.out.print(" empty-list in: ");
//                    System.out.print(pos);
//                    System.out.println();
//                }
                //rest-countval
                for (int j = 0; j < ko; j++) {
                    countval.put(j, 0);
                }
                //reset-possibilidades
                for (int j = pos; j < len; j++) {
                    List<Integer> lis = possibilidades.get(j);
                    lis.clear();
                    lis.addAll(targetvList);
                }
                for (int i = 0; i < arr.size(); i++) {
                    posicoesExcluidas = mapExcludePosition.get(i);
                    int post = i;
                    int valt = arr.get(i);
                    //roolback está vindo true... Verificar isso
                    clearEmptyCombination(post, valt, countval, maxValCount, possibilidades, posicoesExcluidas, false);
                }
                possibilidades.get(arr.size()).remove(valRollback);
                roolback = false;
                continue;
            }
            Integer val = list.poll();
            arr.add(val);
            roolback = clearEmptyCombination(pos, val, countval, maxValCount, possibilidades, posicoesExcluidas);
            if (pos >= longest) {
                longest = pos;
                System.out.print("arr[");
                System.out.print(longest);
                System.out.print("]: ");
                System.out.print(arr);
                System.out.println();
            }
        }

        if (arr.size() < len) {
            throw new IllegalStateException("Combination impossible");
        }
        System.out.print("\nCombinação:");
        System.out.println(arr);

        boolean test = checkSequence(k, arr.toArray(new Integer[0]));
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }
        assertTrue("Combination invalid", test);
    }

    public void testCheckSequence() {
        System.out.println("check big sequence");
        int k = 57;
        int ko = k - 2;
        Integer[] seq = new Integer[]{34, 15, 13, 24, 51, 40, 32, 7, 54, 48, 10, 30, 37, 39, 28, 70, 2, 95, 94, 6, 20, 21, 4, 23, 62, 25, 47, 103, 36, 89, 150, 31, 53, 12, 52, 35, 102, 158, 38, 49, 205, 107, 79, 104, 44, 78, 93, 85, 213, 1, 11, 106, 162, 163, 164, 218, 42, 97, 260, 273, 203, 76, 264, 45, 61, 157, 59, 17, 41, 75, 263, 117, 82, 68, 46, 148, 65, 437, 219, 27, 268, 312, 18, 353, 19, 149, 123, 8, 135, 315, 258, 120, 112, 168, 74, 92, 33, 87, 188, 383, 131, 181, 175, 66, 144, 113, 80, 216, 140, 9, 86, 400, 227, 84, 186, 99, 313, 274, 198, 314, 90, 50, 172, 0, 215, 155, 3, 125, 5, 108, 96, 535, 100, 60, 261, 130, 67, 202, 193, 154, 55, 299, 230, 29, 328, 121, 151, 326, 83, 137, 206, 156, 160, 204, 209, 16, 167, 159, 297, 214, 379, 184, 118, 26, 241, 368, 210, 325, 194, 169, 211, 319, 493, 132, 425, 380, 189, 64, 287, 14, 490, 143, 180, 77, 243, 153, 245, 134, 311, 212, 279, 22, 370, 192, 285, 161, 43, 473, 72, 321, 257, 231, 269, 478, 139, 247, 239, 235, 183, 588, 138, 238, 177, 232, 480, 152, 327, 237, 98, 265, 539, 329, 244, 81, 323, 176, 429, 126, 71, 324, 477, 116, 438, 294, 248, 316, 122, 91, 124, 217, 224, 208, 127, 165, 179, 171, 377, 250, 63, 200, 397, 142, 266, 56, 129, 369, 178, 270, 234, 57, 303, 334, 434, 486, 249, 242, 190, 305, 304, 271, 302, 267, 272, 254, 145, 182, 146, 281, 136, 322, 147, 384, 309, 233, 173, 282, 290, 300, 253, 381, 199, 435, 196, 298, 402, 364, 374, 548, 73, 255, 259, 115, 603, 487, 395, 533, 251, 408, 382, 201, 371, 426, 344, 296, 431, 376, 114, 292, 419, 352, 361, 389, 306, 494, 484, 439, 101, 404, 433, 109, 399, 658, 277, 355, 347, 359, 713, 286, 700, 768, 345, 432, 414, 222, 337, 185, 596, 197, 226, 288, 367, 358, 392, 340, 141, 545, 240, 262, 590, 645, 457, 357, 351, 349, 342, 409, 88, 600, 378, 416, 170, 133, 541, 110, 492, 363, 320, 289, 450, 225, 119, 58, 407, 69, 418, 375, 191, 423, 187, 308, 455, 542, 256, 128, 293, 549, 195, 341, 462, 111, 207, 413, 252, 354, 823, 481, 459, 343, 236, 452, 105, 422, 474, 643, 223, 424, 547, 360, 512, 291, 510, 415, 651, 465, 220, 295, 346, 468, 396, 505, 523, 578, 706, 565, 471, 597, 514, 488, 463, 464, 652, 470, 602, 469, 529, 246, 318, 604, 373, 519, 516, 366, 336, 444, 659, 532, 517, 698, 518, 391, 511, 589, 278, 528, 499, 878, 489, 348, 317, 707, 544, 412, 174, 675, 627, 401, 280, 275, 447, 746, 330, 454, 394, 560, 581, 428, 572, 554, 467, 655, 332, 657, 166, 571, 390, 664, 958, 483, 356, 307, 387, 406, 649, 543, 445, 817, 682, 620, 714, 436, 526, 766, 574, 410, 456, 385, 567, 403, 583, 562, 538, 639, 461, 710, 301, 573, 398, 442, 283, 221, 451, 615, 593, 584, 350, 762, 335, 712, 642, 628, 372, 566, 421, 430, 719, 362, 759, 466, 458, 636, 536, 633, 284, 417, 534, 502, 704, 472, 599, 691, 546, 339, 440, 730, 872, 648, 405, 731, 229, 594, 579, 333, 331, 785, 524, 495, 761, 228, 503, 703, 569, 688, 491, 654, 453, 500, 816, 449, 485, 507, 427, 809, 660, 638, 598, 609, 476, 540, 386, 513, 709, 601, 521, 793, 587, 765, 692, 933, 903, 630, 813, 501, 388, 758, 798, 610, 978, 656, 612, 848, 564, 769, 820, 1043, 509, 774, 624, 693, 684, 575, 711, 508, 840, 871, 856, 520, 663, 522, 743, 310, 694, 577, 737, 667, 988, 670, 739, 504, 531, 527, 420, 868, 411, 448, 586, 662, 276, 441, 752, 697, 443, 559, 683, 482, 676, 1013, 557, 821, 621, 634, 858, 525, 497, 552, 653, 749, 829, 884, 591, 515, 498, 679, 685, 748, 672, 644, 755, 727, 592, 814, 551, 537, 553, 561, 1098, 753, 699, 632, 641, 530, 506, 496, 722, 754, 558, 930, 803, 876, 576, 580, 708, 623, 734, 669, 619, 608, 895, 807, 810, 393, 570, 629, 635, 550, 582, 614, 646, 931, 475, 338, 690, 460, 665, 923, 607, 446, 681, 744, 777, 950, 782, 911, 556, 853, 873, 968, 738, 720, 605, 763, 791, 764, 908, 740, 808, 920, 677, 1017, 772, 939, 671, 875, 963, 618, 640, 801, 622, 678, 717, 745, 617, 563, 1068, 847, 975, 818, 927, 832, 795, 626, 824, 837, 786, 775, 616, 637, 865, 879, 613, 673, 1030, 800, 804, 365, 724, 568, 985, 689, 736, 830, 650, 701, 705, 725, 1040, 595, 1033, 913, 773, 726, 982, 728, 864, 1005, 828, 1095, 729, 781, 631, 892, 794, 819, 732, 479, 674, 696, 741, 792, 787, 686, 606, 668, 789, 784, 723, 874, 947, 555, 760, 1002, 625, 849, 733, 1037, 1123, 836, 846, 1150, 981, 585, 702, 972, 661, 1023, 771, 827, 779, 1024, 901, 949, 783, 838, 862, 826, 904, 919, 796, 1253, 788, 1166, 869, 929, 751, 695, 897, 1018, 863, 841, 902, 942, 984, 957, 1049, 1178, 934, 915, 851, 994, 1085, 756, 834, 881, 715, 887, 885, 687, 747, 716, 780, 989, 914, 850, 917, 974, 1198, 839, 757, 896, 918, 1143, 906, 1088, 894, 882, 835, 806, 1104, 1153, 1044, 1003, 951, 1205, 1060, 770, 889, 1140, 647, 842, 997, 1042, 956, 812, 1115, 893, 859, 811, 986, 959, 1029, 870, 928, 1006, 1260, 855, 924, 1159, 944, 1162, 983, 948, 1036, 1001, 1039, 979, 866, 799, 998, 1315, 721, 1233, 833, 1091, 938, 860, 867, 1217, 1021, 1288, 797, 1027, 969, 844, 966, 890, 825, 1056, 778, 925, 952, 898, 1208, 1057, 980, 953, 891, 999, 921, 805, 977, 922, 976, 1052, 954, 1012, 1028, 909, 1118, 1214, 905, 888, 1061, 1096, 940, 991, 1032, 899, 1256, 1014, 936, 1009, 742, 880, 1170, 926, 1008, 767, 943, 1099, 1146, 1082, 970, 1195, 611, 1259, 1201, 1094, 680, 1011, 1038, 1078, 1151, 1025, 1149, 1148, 995, 1064, 1250, 883, 1076, 1058, 852, 1089, 718, 1071, 1107, 964, 1059, 973, 843, 1215, 1119, 1016, 1114, 907, 1112, 1137, 945, 1173, 992, 1050, 1370, 1066, 1133, 1063, 1228, 961, 1041, 1113, 1105, 1258, 802, 1053, 1192, 1048, 993, 1101, 854, 1020, 1313, 1343, 1154, 735, 1067, 932, 1093, 822, 960, 831, 1019, 937, 1167, 1034, 1000, 1206, 1074, 857, 1169, 1054, 1084, 815, 1363, 1131, 861, 1247, 941, 1069, 1188, 1222, 1204, 886, 1035, 1418, 1302, 1129, 1055, 845, 1109, 946, 1031, 1004, 1272, 1311, 935, 1263, 1110, 1108, 1283, 1083, 996, 1168, 910, 1121, 965, 987, 990, 1408, 1007, 962, 1087, 1203, 971, 1147, 1139, 1186, 750, 916, 1111, 1308, 1298, 666, 1368, 1097, 1398, 1051, 1015, 1241, 1160, 1353, 1124, 1243, 1194, 877, 1453, 1142, 1079, 1202, 776, 1357, 1164, 1305, 1062, 1117, 1277, 790, 1134, 1090, 1145, 1197, 1209, 1332, 1264, 1046, 1360, 1092, 1144, 1249, 1314, 1047, 1261, 1126, 1072, 1423, 1200, 1156, 1199, 900, 1138, 1319, 1106, 1070, 1193, 1174, 1304, 1165, 1102, 1316, 1257, 955, 1157, 1296, 1369, 1416, 1181, 1172, 1045, 1224, 1254, 1279, 1248, 1419, 1196, 1303, 1155, 1116, 1080, 1225, 1333, 1211, 1318, 1252, 1075, 1207, 1266, 1171, 1010, 1176, 1086, 1425, 1231, 1125, 1216, 1262, 912, 1232, 1309, 1229, 1219, 1141, 1179, 1236, 1073, 1227, 1103, 1128, 1480, 1182, 1327, 1373, 1281, 1424, 1177, 1135, 1535, 1371, 1065, 1210, 1234, 967, 1426, 1374, 1291, 1226, 1447, 1376, 1325, 1286, 1120, 1335, 1175, 1158, 1223, 1337, 1312, 1161, 1270, 1152, 1483, 1334, 1026, 1127, 1481, 1220, 1284, 1163, 1336, 1367, 1389, 1189, 1184, 1100, 1183, 1390, 1213, 1130, 1364, 1307, 1478, 1180, 1278, 1380, 1022, 1429, 1122, 1191, 1221, 1239, 1274, 1414, 1321, 1339, 1359, 1273, 1185, 1077, 1378, 1245, 1244, 1346, 1294, 1265, 1268, 1382, 1271, 1237, 1326, 1392, 1428, 1292, 1255, 1366, 1401, 1310, 1387, 1230, 1306, 1482, 1391, 1412, 1490, 1508, 1289, 1320, 1439, 1536, 1431, 1430, 1344, 1331, 1375, 1287, 1365, 1323, 1267, 1466, 1479, 1330, 1388, 1275, 1420, 1499, 1381, 1276, 1341, 1240, 1235, 1442, 1415, 1497, 1251, 1475, 1465, 1290, 1295, 1132, 1386, 1530, 1396, 1469, 1329, 1534, 1449, 1081, 1187, 1384, 1362, 1350, 1409, 1338, 1507, 1322, 1518, 1324, 1345, 1446, 1361, 1402, 1422, 1394, 1349, 1342, 1405, 1218, 1463, 1238, 1472, 1301, 1280, 1393, 1293, 1340, 1538, 1354, 1400, 1242, 1435, 1421, 1460, 1317, 1417, 1300, 1406, 1285, 1299, 1212, 1297, 1516, 1355, 1328, 1377, 1352, 1527, 1444, 1413, 1477, 1452, 1443, 1358, 1476, 1533, 1246, 1502, 1468, 1136, 1437, 1474, 1461, 1427, 1347, 1473, 1410, 1269, 1455, 1457, 1467, 1456, 1190, 1438, 1404, 1459, 1397, 1484, 1451, 1385, 1432, 1395, 1407, 1520, 1470, 1379, 1504, 1519, 1372, 1282, 1450, 1348, 1492, 1440, 1537, 1528, 1485, 1383, 1539, 1488, 1448, 1511, 1525, 1351, 1434, 1523, 1506, 1411, 1515, 1471, 1403, 1464, 1441, 1514, 1486, 1462, 1524, 1532, 1501, 1526, 1487, 1505, 1433, 1531, 1493, 1512, 1436, 1454, 1494, 1491, 1529, 1489, 1356, 1509, 1458, 1521, 1503, 1498, 1513, 1445, 1500, 1495, 1399, 1522, 1517, 1510, 1496};
        //Traduzir sequencia
        System.out.println("arr[" + seq.length + "]=");
        for (int i = 0; i < seq.length; i++) {
            seq[i] = seq[i] % ko;
            System.out.print(seq[i]);
            System.out.print(", ");
        }
        System.out.println();

        boolean test = checkSequence(k, seq);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }
//        assertTrue("Combination invalid", test);
    }

    private boolean clearEmptyCombination(Integer pos, Integer val, Map<Integer, Integer> countval, int maxValCount, Map<Integer, List<Integer>> possibilidades, List<Integer> posicoesExcluidas) {
        return clearEmptyCombination(pos, val, countval, maxValCount, possibilidades, posicoesExcluidas, true);
    }

    private boolean clearEmptyCombination(Integer pos, Integer val, Map<Integer, Integer> countval, int max_val_count, Map<Integer, List<Integer>> possibilidades, List<Integer> posicoesExcluidas, boolean failEmpty) {
        boolean roolback = false;
        int len = possibilidades.size();
        int divPoint = Collections.binarySearch(posicoesExcluidas, pos);
        if (divPoint < 0) {
            divPoint = (-(divPoint) - 1);
        }
        countval.put(val, countval.get(val));
        // Remover val das futuras listas de possibilidade
        if (countval.get(val) >= max_val_count) {
            for (int i = pos; i < len; i++) {
                List<Integer> possiPosi = possibilidades.get(i);
                possiPosi.remove(val);
                if (failEmpty && possiPosi.isEmpty()) {
                    roolback = true;
                    break;
                }
            }
        }
        for (int i = divPoint; i < posicoesExcluidas.size(); i++) {
            Integer posicao = posicoesExcluidas.get(i);
            List<Integer> possiPosi = possibilidades.get(posicao);
            possiPosi.remove(val);
            if (failEmpty && possiPosi.isEmpty()) {
                roolback = true;
                break;
            }
        }
        return roolback;
    }

    private boolean checkSequence(int k, Integer[] arr) {
        boolean ret = true;
        int len = arr.length;
        int ko = k - 2;
        int maxcount = len / ko;
        int[] arrup = new int[len];
        int[] arrdown = new int[len];
        int[] countval = new int[ko];
        int offsetup = ko - 1;
        int up = 0;
        int down = 1;

        for (int i = 0; i < len; i++) {
            arrup[i] = up;
            arrdown[i] = down++;
            if (i == offsetup) {
                up++;
                offsetup += (ko - up);
            }
            if (down == ko + 1) {
                down = up + 1;
            }
            if (i < ko) {
                countval[i] = 0;
            }
        }
        for (int i = 0; i < len; i++) {
            int val = arr[i];
            boolean max = countval[val] <= maxcount;
            boolean retlocal = true;
            retlocal = retlocal && max;
            up = arrup[i];
            down = arrdown[i];
            int j = 0;
            for (j = 0; j < i && retlocal; j++) {
                if (arrdown[j] == up || arrdown[j] == down || arrup[j] == up) {
                    retlocal = retlocal && arr[j] != val;
                }
            }
            if (!retlocal) {
                if (!max) {
                    System.out.println("Max count exceded: " + val + " " + countval[val]);
                }
                System.out.println("Value " + val + " Failed in position: " + i + " conflict " + (j - 1));
            }
            countval[val]++;
            ret = ret && retlocal;
        }
        return ret;
    }

    private boolean exclude(int[] arrup, int[] arrdown, int[] arr, int pos, int val) {
        int up = arrup[pos];
        int down = arrdown[pos];
        boolean ret = false;
        for (int i = 0; i < pos && !ret; i++) {
            if (arrdown[i] == up || arrdown[i] == down || arrup[i] == up) {
                ret = ret || arr[i] == val;
            }
        }
        return ret;
    }
}
