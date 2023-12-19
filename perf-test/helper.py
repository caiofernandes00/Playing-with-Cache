from datetime import datetime

class Helper:
    def should_retrieve_from_cache(self, use_chace, no_cache, max_age, data_from_cache, last_time_fetched):
        should_it = use_chace and not no_cache and data_from_cache is not None and self.check_if_cache_is_stale(last_time_fetched, max_age)
        print(">>> Retrieved from cache..." if should_it else ">>> Retrieved from server...")
        return should_it

    def check_if_cache_is_stale(self, last_time_fetched, max_age):
        if last_time_fetched is None or max_age is None:
            return True

        current_time = datetime.utcnow()
        time_elapsed_in_seconds = (
            (current_time.timestamp() - last_time_fetched.timestamp())
            if isinstance(last_time_fetched, datetime)
            else 0
        )

        return time_elapsed_in_seconds <= max_age
