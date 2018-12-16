import java.util.Random;

/**
 * Created by esalman17 on 16.12.2018.
 */

public class DynamicPath {
    int u, v; // Node ids to connect
    int cost;
    Random rand = new Random();

    DynamicPath(int u, int v){
        if(u > v){
            int temp = u;
            u = v;
            v = temp;
        }
        this.u = u;
        this.v = v;
        rand.setSeed(u); // TODO for debug
        cost = rand.nextInt(10) + 1;
    }

    public boolean updateCost(){
        int r = rand.nextInt(20) + 1;
        if(r <= 10){
            String s = String.format("Path cost between %d - %d is changed %d -> %d", u,v,cost,r);
            System.out.println(s);
            cost = r;
            return true;
        }
        else{
            // Cost did not updated
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DynamicPath that = (DynamicPath) o;

        if (u != that.u) return false;
        return v == that.v;
    }

    @Override
    public int hashCode() {
        int result = u;
        result = 31 * result + v;
        return result;
    }
}
