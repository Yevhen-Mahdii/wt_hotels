package ua.edu.hotels.data.dto;

import ua.edu.hotels.data.entity.*;

/** Signals whether the first administrator still needs to be created. */
public record SetupStatus(boolean setupRequired) {}
