package ru.practicum.exception;

public enum ErrorMessages {

    NOT_FOUND("%s with id=%s was not found"),
    INTEGRITY_VIOLATION("%s already exists"),
    CATEGORY_NOT_EMPTY("The category is not empty"),
    REQUEST_ALREADY_SENT("User with id=%s has already sent a request to event id=%s"),
    INITIATOR_CANNOT_PARTICIPATE("Event initiator cannot participate in their own event"),
    UNPUBLISHED_EVENT("Cannot participate in an unpublished event"),
    PARTICIPANT_LIMIT_REACHED("The participant limit has been reached"),
    NOT_EVENT_INITIATOR("User with id=%s is not the initiator of event id=%s"),
    REQUEST_NOT_PENDING("Status can only be changed for requests with PENDING status"),
    EVENT_NOT_PENDING_OR_CANCELED("Only pending or canceled events can be changed"),
    EVENT_DATE_TOO_SOON("Event date must be at least 1 hours from now"),
    CANNOT_PUBLISH_EVENT("Cannot publish the event because it's not in the right state: %s"),
    CANNOT_REJECT_PUBLISHED_EVENT("Cannot reject the event because it's already published");

    private final String template;

    ErrorMessages(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        return args.length == 0 ? template : String.format(template, args);
    }
}
