package me.meenagopal24.recyclerdemo;

import java.util.List;

public class Category {
    class item {
        String name ,image;

        public item(String name, String image) {
            this.name = name;
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        @Override
        public String toString() {
            return "item{" +
                    "name='" + name + '\'' +
                    ", image='" + image + '\'' +
                    '}';
        }
    }
    List<item> result;

    public Category(List<item> result) {
        this.result = result;
    }

    public List<item> getResult() {
        return result;
    }

    public void setResult(List<item> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Category{" +
                "result=" + result +
                '}';
    }
}
