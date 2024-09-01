import { createRouter, createWebHistory } from "vue-router";
import LoginView from "@/view/LoginView.vue";
import CalendarView from "@/view/CalendarView.vue";

const routes = [
    {
        path: "/",
        name: "login",
        component: LoginView,
    },
    {
        path: "/calendar",
        name: "calendar",
        component: CalendarView,
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;