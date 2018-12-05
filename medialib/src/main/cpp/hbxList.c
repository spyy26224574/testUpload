#include "hbxList.h"

inline void __list_add(struct list_head *item, struct list_head *prev, struct list_head *next) {
    item->next = next;
    prev->next = item;
}

void list_add(struct list_head *item, struct list_head *prev) {
    item->next = (struct list_head *) 0;
    prev->next = item;
}

void list_add_tail(struct list_head *item, struct list_head *head) {
    struct list_head *temp = head;
    item->next = (struct list_head *) 0;
    do {
        if (temp->next)
            temp = temp->next;
    } while (temp->next);
    __list_add(item, temp, temp->next);
}

void __list_del(struct list_head *prev, struct list_head *next) {
    prev->next = next;
}

void list_del(struct list_head *item, struct list_head *head) {
    if (item)
        __list_del(head, item->next);
}

struct list_head *list_pop(struct list_head *list) {
    struct list_head *item = (struct list_head *) 0;
    if (list) {
        item = list->next;
        if (item) {
            list->next = item->next;
        }
    }
    return item;
}

void list_push(struct list_head *item, struct list_head *list) {
    list_add_tail(item, list);
}

void list_init(struct list_head *item) {
    item->next = (struct list_head *) 0;
};